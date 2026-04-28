package com.project.wms.auth.repository;

import com.project.wms.auth.entity.AuthPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthPermisionRepository extends JpaRepository<AuthPermission, Long> {

    //Ham tim permission bang code
    Optional<AuthPermission> findByCode(String code);

    //Ham kiem tra co ton tai code chua
    boolean existsByCode(String code);
}
