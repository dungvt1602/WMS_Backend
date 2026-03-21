package com.project.wms.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.wms.auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Spring sẽ tự tạo câu lệnh: SELECT * FROM auth_role WHERE name = ?
    Optional<Role> findByName(String name);
}
