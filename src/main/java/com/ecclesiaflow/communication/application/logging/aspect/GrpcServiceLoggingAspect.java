package com.ecclesiaflow.communication.application.logging.aspect;

import com.ecclesiaflow.communication.application.logging.SecurityMaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Logging aspect for gRPC service calls.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class GrpcServiceLoggingAspect {

    private static final String MDC_TRACE_ID = "traceId";

    @Pointcut("execution(* com.ecclesiaflow.communication.io.grpc.server.EmailGrpcServiceImpl.*(..))")
    public void grpcServiceOperation() {}

    @Around("grpcServiceOperation()")
    public Object logGrpcServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        try {
            MDC.put(MDC_TRACE_ID, traceId);
            log.debug("GRPC: {} | start | traceId={}", methodName, traceId);

            Object result = joinPoint.proceed();

            log.debug("GRPC: {} | success | traceId={}", methodName, traceId);
            return result;
        } catch (Exception e) {
            log.error("GRPC: {} | failed | traceId={} | reason={}",
                    methodName, traceId, SecurityMaskingUtils.rootMessage(e));
            throw e;
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
