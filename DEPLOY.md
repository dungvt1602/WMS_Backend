# 🚀 Hướng Dẫn Triển Khai WMS Backend lên Oracle Kubernetes Engine (OKE)

> **Stack:** Spring Boot Monolith → OKE + Jenkins CI/CD + Vault + PostgreSQL  
> **Chi phí:** $0/tháng (Oracle Always Free Tier)

---

## 📋 Mục Lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Yêu cầu chuẩn bị](#2-yêu-cầu-chuẩn-bị)
3. [Bước 1 — Viết Dockerfile](#3-bước-1--viết-dockerfile)
4. [Bước 2 — Tạo Oracle Cloud Account + OKE Cluster](#4-bước-2--tạo-oracle-cloud-account--oke-cluster)
5. [Bước 3 — Cài Jenkins trên VM Oracle](#5-bước-3--cài-jenkins-trên-vm-oracle)
6. [Bước 4 — Viết K8s Manifests](#6-bước-4--viết-k8s-manifests)
7. [Bước 5 — Cài HashiCorp Vault trên OKE](#7-bước-5--cài-hashicorp-vault-trên-oke)
8. [Bước 6 — Viết Jenkinsfile CI/CD](#8-bước-6--viết-jenkinsfile-cicd)
9. [Bước 7 — Kết nối Vercel Frontend](#9-bước-7--kết-nối-vercel-frontend)
10. [Lộ trình nâng lên Microservice](#10-lộ-trình-nâng-lên-microservice)
11. [Checklist tổng hợp](#11-checklist-tổng-hợp)

---

## 1. Tổng Quan Kiến Trúc

```
Developer (Local)
      │
      │ git push
      ▼
   GitHub
      │
      │ webhook trigger
      ▼
┌─────────────────────────────────────────────────────┐
│                  ORACLE CLOUD FREE                  │
│                                                     │
│  VM 1 (AMD - Always Free)                           │
│  └── Jenkins (CI/CD Server)                         │
│        │ docker build + push                        │
│        │ kubectl apply                              │
│        ▼                                            │
│  OKE Cluster (ARM 4CPU/24GB - Always Free)          │
│  ┌──────────────────────────────────────────────┐   │
│  │  namespace: production                       │   │
│  │                                              │   │
│  │  Pod: wms-backend (Spring Boot :8080)        │   │
│  │  Pod: postgresql (:5432)                     │   │
│  │  Pod: vault (:8200)                          │   │
│  │  Pod: ingress-nginx (gateway)                │   │
│  └──────────────────────────────────────────────┘   │
│                                                     │
│  Load Balancer (IP Public miễn phí)                 │
└─────────────────────────────────────────────────────┘
      │
      │ HTTPS API calls
      ▼
Vercel (Frontend - $0)
      │
      ▼
   End User
```

**Chi phí tổng:**
| Thành phần | Nền tảng | Chi phí |
|---|---|---|
| OKE Cluster | Oracle Always Free | $0 |
| Jenkins VM | Oracle Always Free | $0 |
| Frontend | Vercel | $0 |
| Docker Registry | GitHub Container Registry | $0 |
| Domain (tuỳ chọn) | Namecheap/Porkbun | ~$10/năm |

---

## 2. Yêu Cầu Chuẩn Bị

### Máy local cần có:

- [ ] Docker Desktop đã cài
- [ ] kubectl đã cài
- [ ] Git đã cài
- [ ] Java 17+ (để build local)
- [ ] Maven / Gradle

### Tài khoản cần tạo:

- [ ] Oracle Cloud account (https://cloud.oracle.com) — dùng thẻ Visa/Mastercard để verify, không bị charge
- [ ] GitHub account
- [ ] Docker Hub hoặc dùng GHCR (GitHub Container Registry)

---

## 3. Bước 1 — Viết Dockerfile

Tạo file `Dockerfile` ở root project WMS:

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Tạo user non-root (bảo mật)
RUN addgroup -S wmsgroup && adduser -S wmsuser -G wmsgroup
USER wmsuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Test build local:

```bash
# Build image
docker build -t wms-backend:local .

# Run thử
docker run -p 8080:8080 \
  -e JWT_SECRET=test_secret_local \
  -e SPRING_PROFILES_ACTIVE=dev \
  wms-backend:local

# Kiểm tra
curl http://localhost:8080/actuator/health
```

---

## 4. Bước 2 — Tạo Oracle Cloud Account + OKE Cluster

### 4.1 Tạo Oracle Cloud Account

1. Vào https://cloud.oracle.com → **Start for free**
2. Điền thông tin, verify thẻ Visa (không bị charge)
3. Chọn **Home Region:** `ap-singapore-1` (gần VN nhất, free tier)

### 4.2 Tạo OKE Cluster

1. Oracle Console → **Developer Services** → **Kubernetes Clusters (OKE)**
2. Click **Create Cluster**
3. Chọn **Quick Create**
4. Cấu hình:
   ```
   Name: wms-cluster
   Kubernetes Version: 1.29.x (mới nhất)
   Node Shape: VM.Standard.A1.Flex (ARM - FREE)
   OCPUs: 4
   Memory: 24 GB
   Node Count: 2
   ```
5. Click **Create** → chờ ~10 phút

### 4.3 Kết nối kubectl từ máy local

```bash
# Cài OCI CLI
pip install oci-cli

# Cấu hình OCI CLI
oci setup config

# Download kubeconfig từ Oracle Console
# Developer Services → OKE → wms-cluster → Access Cluster
# Copy lệnh oci ce cluster create-kubeconfig ... và chạy

# Test kết nối
kubectl get nodes
# Output: 2 nodes ở trạng thái Ready ✅
```

---

## 5. Bước 3 — Cài Jenkins Trên VM Oracle

### 5.1 Tạo VM cho Jenkins

1. Oracle Console → **Compute** → **Instances** → **Create Instance**
2. Cấu hình:
   ```
   Name: jenkins-server
   Shape: VM.Standard.E2.1.Micro (AMD - Always Free)
   OS: Ubuntu 22.04
   ```
3. Tạo SSH key, download file `.pem`

### 5.2 SSH vào VM và cài Jenkins

```bash
# SSH vào VM
ssh -i your-key.pem ubuntu@<VM_PUBLIC_IP>

# Cài Java
sudo apt update
sudo apt install -y openjdk-17-jdk

# Cài Jenkins
curl -fsSL https://pkg.jenkins.io/debian/jenkins.io-2023.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install -y jenkins

# Cài Docker
sudo apt install -y docker.io
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Cài kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Lấy Jenkins initial password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 5.3 Cấu hình Jenkins

1. Truy cập `http://<VM_IP>:8080`
2. Nhập initial password
3. Cài plugins: **Git, Docker Pipeline, Kubernetes CLI, GitHub Integration**
4. Thêm Credentials:
   ```
   Kind: Username with password
   ID: ghcr-credentials
   Username: <github_username>
   Password: <github_personal_access_token>
   ```
5. Copy kubeconfig vào Jenkins server:
   ```bash
   sudo mkdir -p /var/lib/jenkins/.kube
   sudo cp ~/.kube/config /var/lib/jenkins/.kube/config
   sudo chown -R jenkins:jenkins /var/lib/jenkins/.kube
   ```

### 5.4 Mở port Jenkins trên Oracle Security List

Oracle Console → Networking → VCN → Security Lists → Add Ingress Rule:

```
Source: 0.0.0.0/0
Protocol: TCP
Port: 8080
```

---

## 6. Bước 4 — Viết K8s Manifests

Tạo thư mục `k8s/` trong project:

```
k8s/
├── namespace.yaml
├── deployment.yaml
├── service.yaml
├── ingress.yaml
├── configmap.yaml
└── secret.yaml
```

### namespace.yaml

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
```

### configmap.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: wms-config
  namespace: production
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
```

### secret.yaml

> ⚠️ File này KHÔNG commit lên Git. Dùng Vault thay thế ở production.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: wms-secrets
  namespace: production
type: Opaque
stringData:
  jwt-secret: "THAY_BANG_SECRET_THAT"
  db-username: "wms_user"
  db-password: "THAY_BANG_PASSWORD_THAT"
```

### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wms-backend
  namespace: production
  labels:
    app: wms-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: wms-backend
  template:
    metadata:
      labels:
        app: wms-backend
    spec:
      containers:
        - name: wms-backend
          image: ghcr.io/YOUR_GITHUB_USERNAME/wms-backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: wms-secrets
                  key: jwt-secret
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: wms-secrets
                  key: db-username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: wms-secrets
                  key: db-password
          envFrom:
            - configMapRef:
                name: wms-config
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1024Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
```

### service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: wms-backend-svc
  namespace: production
spec:
  selector:
    app: wms-backend
  ports:
    - port: 80
      targetPort: 8080
  type: LoadBalancer
```

### Apply manifests:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Kiểm tra
kubectl get all -n production
```

---

## 7. Bước 5 — Cài HashiCorp Vault Trên OKE

### 7.1 Cài Vault bằng Helm

```bash
# Cài Helm (nếu chưa có)
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Thêm Vault Helm repo
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

# Cài Vault vào OKE
helm install vault hashicorp/vault \
  --namespace vault \
  --create-namespace \
  --set "server.dev.enabled=false" \
  --set "server.standalone.enabled=true"

# Kiểm tra
kubectl get pods -n vault
```

### 7.2 Khởi tạo Vault

```bash
# Exec vào Vault pod
kubectl exec -it vault-0 -n vault -- vault operator init

# Lưu lại Unseal Keys và Root Token (RẤT QUAN TRỌNG)
# Unseal Vault (cần làm mỗi khi Vault restart)
kubectl exec -it vault-0 -n vault -- vault operator unseal <UNSEAL_KEY_1>
kubectl exec -it vault-0 -n vault -- vault operator unseal <UNSEAL_KEY_2>
kubectl exec -it vault-0 -n vault -- vault operator unseal <UNSEAL_KEY_3>
```

### 7.3 Lưu WMS secrets vào Vault

```bash
# Login Vault
kubectl exec -it vault-0 -n vault -- vault login <ROOT_TOKEN>

# Enable KV secrets engine
kubectl exec -it vault-0 -n vault -- vault secrets enable -path=secret kv-v2

# Lưu secrets cho WMS
kubectl exec -it vault-0 -n vault -- vault kv put secret/wms \
  jwt.secret="your_jwt_secret_here" \
  spring.datasource.username="wms_user" \
  spring.datasource.password="your_db_password_here"

# Kiểm tra
kubectl exec -it vault-0 -n vault -- vault kv get secret/wms
```

### 7.4 Cấu hình Spring Boot đọc từ Vault

Thêm dependency vào `pom.xml`:

```xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```

Tạo `application-prod.properties`:

```properties
spring.config.import=vault://
spring.cloud.vault.uri=http://vault.vault.svc.cluster.local:8200
spring.cloud.vault.authentication=TOKEN
spring.cloud.vault.token=${VAULT_TOKEN}
spring.cloud.vault.kv.enabled=true
spring.cloud.vault.kv.backend=secret
spring.cloud.vault.kv.default-context=wms
```

---

## 8. Bước 6 — Viết Jenkinsfile CI/CD

Tạo file `Jenkinsfile` ở root project:

```groovy
pipeline {
    agent any

    environment {
        IMAGE_NAME = "ghcr.io/YOUR_GITHUB_USERNAME/wms-backend"
        IMAGE_TAG = "${BUILD_NUMBER}"
        NAMESPACE = "production"
        DEPLOYMENT = "wms-backend"
    }

    stages {

        stage('📥 Checkout') {
            steps {
                git branch: 'main',
                        url: 'https://github.com/YOUR_GITHUB_USERNAME/wms.git'
            }
        }

        stage('🔨 Build & Test') {
            steps {
                sh './mvnw clean package'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('🐳 Docker Build') {
            steps {
                sh """
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                """
            }
        }

        stage('📤 Docker Push') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'ghcr-credentials',
                        usernameVariable: 'USERNAME',
                        passwordVariable: 'PASSWORD'
                )]) {
                    sh """
                        echo \$PASSWORD | docker login ghcr.io -u \$USERNAME --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('🚀 Deploy to OKE') {
            steps {
                sh """
                    kubectl set image deployment/${DEPLOYMENT} \
                        ${DEPLOYMENT}=${IMAGE_NAME}:${IMAGE_TAG} \
                        --namespace=${NAMESPACE}

                    kubectl rollout status deployment/${DEPLOYMENT} \
                        --namespace=${NAMESPACE} \
                        --timeout=300s
                """
            }
        }

        stage('✅ Verify') {
            steps {
                sh """
                    kubectl get pods -n ${NAMESPACE} -l app=${DEPLOYMENT}
                    kubectl get svc -n ${NAMESPACE}
                """
            }
        }
    }

    post {
        success {
            echo '✅ Deploy thành công! Build #${BUILD_NUMBER}'
        }
        failure {
            echo '❌ Deploy thất bại! Kiểm tra log Jenkins.'
        }
        always {
            sh 'docker system prune -f'
        }
    }
}
```

### Cấu hình GitHub Webhook:

1. GitHub repo → **Settings** → **Webhooks** → **Add webhook**
2. Payload URL: `http://<JENKINS_VM_IP>:8080/github-webhook/`
3. Content type: `application/json`
4. Events: **Just the push event**

---

## 9. Bước 7 — Kết Nối Vercel Frontend

### 9.1 Lấy IP public của Backend

```bash
kubectl get svc wms-backend-svc -n production
# EXTERNAL-IP: xxx.xxx.xxx.xxx  ← đây là IP public của bạn
```

### 9.2 Cấu hình Frontend trên Vercel

1. Vercel Dashboard → Project → **Settings** → **Environment Variables**
2. Thêm:
   ```
   VITE_API_BASE_URL = https://api.yourdomain.com
   # hoặc dùng IP tạm:
   VITE_API_BASE_URL = http://xxx.xxx.xxx.xxx/api
   ```

### 9.3 Cấu hình CORS trong Spring Boot

```java

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://your-app.vercel.app",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

---

## 10. Lộ Trình Nâng Lên Microservice

Khi WMS monolith đã chạy ổn định trên OKE, tách dần từng service:

```
Phase 1 — Monolith trên OKE (hiện tại)
└── 1 Deployment: wms-backend

Phase 2 — Tách Auth Service
├── Deployment: wms-auth-service
└── Deployment: wms-backend (bỏ auth logic)

Phase 3 — Tách Inventory Service
├── Deployment: wms-auth-service
├── Deployment: wms-inventory-service
└── Deployment: wms-backend (core còn lại)

Phase 4 — Full Microservice
├── Deployment: wms-api-gateway (Kong/Spring Cloud Gateway)
├── Deployment: wms-auth-service
├── Deployment: wms-inventory-service
├── Deployment: wms-order-service
├── Deployment: wms-report-service
└── Deployment: wms-notification-service

Mỗi service:
- Repo riêng trên GitHub
- Jenkinsfile riêng
- Database riêng (1 DB per service)
- Pod riêng trên OKE
```

---

## 11. Checklist Tổng Hợp

### Giai đoạn 1 — Chuẩn bị (Tuần 1)

- [ ] Viết Dockerfile + test build local
- [ ] Thêm `/actuator/health` endpoint vào Spring Boot
- [ ] Push code lên GitHub
- [ ] Tạo Oracle Cloud account

### Giai đoạn 2 — Setup hạ tầng (Tuần 2)

- [ ] Tạo OKE Cluster (ARM Free)
- [ ] Tạo Jenkins VM (AMD Free)
- [ ] Cài Jenkins + Docker + kubectl trên VM
- [ ] Kết nối kubectl → OKE từ Jenkins

### Giai đoạn 3 — Deploy đầu tiên (Tuần 3)

- [ ] Viết K8s manifests (namespace, deployment, service)
- [ ] Apply manifests lên OKE
- [ ] Verify pod chạy OK
- [ ] Truy cập được API qua IP public

### Giai đoạn 4 — CI/CD (Tuần 4)

- [ ] Viết Jenkinsfile
- [ ] Tạo Jenkins Pipeline job
- [ ] Cấu hình GitHub Webhook
- [ ] Test pipeline: push code → auto deploy

### Giai đoạn 5 — Secret Manager (Tuần 5)

- [ ] Cài Vault trên OKE bằng Helm
- [ ] Init + unseal Vault
- [ ] Lưu WMS secrets vào Vault
- [ ] Cấu hình Spring Boot đọc từ Vault
- [ ] Xoá K8s Secret thủ công, dùng Vault hoàn toàn

### Giai đoạn 6 — Kết nối Frontend (Tuần 6)

- [ ] Cấu hình CORS trong Spring Boot
- [ ] Set VITE_API_BASE_URL trên Vercel
- [ ] Test end-to-end: Frontend → Backend → DB

---

## 📚 Tài Liệu Tham Khảo

| Tài liệu                | Link                                                                  |
|-------------------------|-----------------------------------------------------------------------|
| OKE Documentation       | https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm           |
| HashiCorp Vault on K8s  | https://developer.hashicorp.com/vault/docs/platform/k8s               |
| Jenkins Pipeline Syntax | https://www.jenkins.io/doc/book/pipeline/syntax                       |
| Spring Cloud Vault      | https://docs.spring.io/spring-cloud-vault/docs/current/reference/html |
| Kubectl Cheatsheet      | https://kubernetes.io/docs/reference/kubectl/cheatsheet               |

---

> 💡 **Tip:** Luôn test trên `Minikube local` trước khi apply lên OKE thật để tránh mất thời gian debug trên cloud.