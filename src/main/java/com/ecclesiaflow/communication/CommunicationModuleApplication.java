package com.ecclesiaflow.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EcclesiaFlow Email Module - Enterprise-grade communication service.
 * <p>
 * This module provides comprehensive communication functionality for the EcclesiaFlow platform:
 * </p>
 * <ul>
 *   <li><strong>Transactional emails</strong> - Confirmations, password resets, notifications</li>
 *   <li><strong>Template management</strong> - Thymeleaf templates with i18n support (FR/EN)</li>
 *   <li><strong>Multi-provider</strong> - Gmail SMTP, SendGrid, AWS SES with automatic failover</li>
 *   <li><strong>Queue-based processing</strong> - RabbitMQ for async, reliable delivery</li>
 *   <li><strong>Email tracking</strong> - Opens, clicks, bounces, delivery status</li>
 *   <li><strong>Rate limiting</strong> - Avoid spam flags and respect provider limits</li>
 *   <li><strong>Observability</strong> - Prometheus metrics, distributed tracing</li>
 * </ul>
 *
 * <p><strong>Architecture Principles:</strong></p>
 * <ul>
 *   <li><strong>Clean Architecture</strong> - 4 layers: Application, Business, IO, Web</li>
 *   <li><strong>Ports & Adapters</strong> - Hexagonal architecture with dependency inversion</li>
 *   <li><strong>Contract-First</strong> - OpenAPI + Protobuf specifications</li>
 *   <li><strong>DTO Projections</strong> - Optimized database queries</li>
 *   <li><strong>SOLID Principles</strong> - Single responsibility, interface segregation</li>
 * </ul>
 *
 * <p><strong>APIs:</strong></p>
 * <ul>
 *   <li><strong>REST</strong> - Port 8082, OpenAPI 3.1.1 documented</li>
 *   <li><strong>gRPC</strong> - Port 9092, Protocol Buffers 3</li>
 * </ul>
 *
 * <p><strong>Integration:</strong></p>
 * <ul>
 *   <li>Members Module - Sends confirmation and welcome emails</li>
 *   <li>Auth Module - Sends password reset emails</li>
 *   <li>Future modules - Generic communication API for all EcclesiaFlow services</li>
 * </ul>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableAspectJAutoProxy
public class CommunicationModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicationModuleApplication.class, args);
    }
}
