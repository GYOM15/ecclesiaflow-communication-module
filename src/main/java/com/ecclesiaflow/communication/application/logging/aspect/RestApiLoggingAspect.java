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
 * Logging aspect for REST API calls.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class RestApiLoggingAspect {

    private static final String MDC_TRACE_ID = "traceId";

    @Pointcut("execution(* com.ecclesiaflow.communication.web.delegate.EmailApiDelegate+.*(..))")
    public void restApiOperation() {}

    @Around("restApiOperation()")
    public Object logRestApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        try {
            MDC.put(MDC_TRACE_ID, traceId);
            log.info("API: {} | start | traceId={}", methodName, traceId);

            Object result = joinPoint.proceed();

            log.info("API: {} | success | traceId={}", methodName, traceId);
            return result;
        } catch (Exception e) {
            log.error("API: {} | failed | traceId={} | reason={}",
                    methodName, traceId, SecurityMaskingUtils.rootMessage(e));
            throw e;
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
