package com.medcore.hms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA Auditing so that @CreatedDate and @LastModifiedDate
 * on BaseEntity are automatically populated by Spring Data.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
