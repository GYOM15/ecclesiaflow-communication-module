package com.ecclesiaflow.communication.application.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Logging aspect for gRPC server lifecycle.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class GrpcServerLoggingAspect {

    @Pointcut("execution(* com.ecclesiaflow.communication.io.grpc.server.GrpcServerConfig.grpcServer(..))")
    public void grpcServerStartup() {}

    @AfterReturning(pointcut = "grpcServerStartup()", returning = "server")
    public void logGrpcServerStartup(JoinPoint joinPoint, Object server) {
        log.info("GRPC: server | started | port=9092 | services=EmailService,Health,Reflection");
    }
}
