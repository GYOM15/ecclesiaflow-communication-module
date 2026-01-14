package com.ecclesiaflow.communication.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email module configuration properties (prefix: ecclesiaflow.mail).
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ecclesiaflow.mail")
public class EmailConfigurationProperties {
    
    private String from;
    private String fromName = "EcclesiaFlow";
}
