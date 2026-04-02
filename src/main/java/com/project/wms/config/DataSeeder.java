package com.project.wms.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.auth.entity.Role;
import com.project.wms.auth.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j // Dùng log thay system.out.println()
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data seeder...");

        // Tạo các quyền dữ liệu
        Set<String> rolesToSeeds = Set.of("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER");

        rolesToSeeds.forEach(rolename -> {
            // kiem tra co ton tai rolename chưa, chưa có thì tạo mới luôn
            if (roleRepository.findByName(rolename).isEmpty()) {
                Role role = Role.builder()
                        .name(rolename)
                        .build();
                roleRepository.save(role);
                log.info("Created role: " + rolename);
            } else {
                log.info(rolename + "Role này đã tồn tại....");
            }
        });

        log.info("Ending data seeder...");

    }

}
