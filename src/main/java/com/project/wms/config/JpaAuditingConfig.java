package com.project.wms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration to enable JPA Auditing.
 * This allows the use of @CreatedDate and @LastModifiedDate annotations in entities.
 */
@Configuration
@EnableJpaAuditing // Activation switch for JPA Auditing features
public class JpaAuditingConfig {

}
