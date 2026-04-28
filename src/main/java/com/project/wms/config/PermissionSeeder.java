package com.project.wms.config;

import com.project.wms.auth.dto.PermissionCode;
import com.project.wms.auth.entity.AuthPermission;
import com.project.wms.auth.entity.PermissionWarehouse;
import com.project.wms.auth.repository.AuthPermisionRepository;
import com.project.wms.auth.repository.PermissionWarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionSeeder implements ApplicationRunner {

    private final AuthPermisionRepository authPermisionRepository;


    //Chay 1 lan khi ung dung khoi dong
    @Override
    public void run(ApplicationArguments args) throws Exception {

        int seeder = 0;
        for (PermissionCode item : PermissionCode.values()) {
            if (authPermisionRepository.existsByCode(item.name())) {
                AuthPermission authPermission = AuthPermission.builder()
                        .code(item.name())
                        .description(item.getDescription())
                        .build();
                authPermisionRepository.save(authPermission);
            }
            seeder++;
            log.info("[SEEDER] Permission them thanh cong: " + item.name());
        }

        //kiem tra co thanh cong bao nhieu
        if (seeder == 0) {
            log.info("[SEEDER] Permission da duoc them, Bo qua");
        } else {
            log.info("So permission duoc them vao {} ", seeder);
        }
    }
}
