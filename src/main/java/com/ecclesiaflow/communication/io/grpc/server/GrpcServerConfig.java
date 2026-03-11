package com.ecclesiaflow.communication.io.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.protobuf.services.HealthStatusManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * gRPC server configuration for communication service.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcServerConfig {

    @Value("${grpc.server.port:9092}")
    private int grpcServerPort;

    @Value("${grpc.server.shutdown-timeout-seconds:30}")
    private int shutdownTimeoutSeconds;

    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @Bean
    public Server grpcServer(
            EmailGrpcServiceImpl emailGrpcService,
            HealthStatusManager healthStatusManager) throws IOException {

        Server server = ServerBuilder.forPort(grpcServerPort)
                .addService(emailGrpcService)
                .addService(healthStatusManager.getHealthService())
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        healthStatusManager.setStatus(
                "ecclesiaflow.email.EmailService",
                io.grpc.health.v1.HealthCheckResponse.ServingStatus.SERVING
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            try {
                if (!server.awaitTermination(shutdownTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        return server;
    }
}
