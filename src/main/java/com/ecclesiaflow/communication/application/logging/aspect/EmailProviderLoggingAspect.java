package com.ecclesiaflow.communication.application.logging.aspect;

import com.ecclesiaflow.communication.application.logging.SecurityMaskingUtils;
import com.ecclesiaflow.communication.business.domain.email.Email;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Logging aspect for communication provider operations.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class EmailProviderLoggingAspect {

    private static final String MDC_PROVIDER = "provider";
    private static final String MDC_EMAIL_ID = "emailId";

    @Pointcut("execution(* com.ecclesiaflow.communication.business.domain.provider.EmailProvider+.send(..))")
    public void providerSendOperation() {}

    @Pointcut("execution(* com.ecclesiaflow.communication.business.domain.provider.EmailProvider+.isAvailable())")
    public void providerAvailabilityCheck() {}

    @Around("providerSendOperation()")
    public Object logProviderSend(ProceedingJoinPoint joinPoint) throws Throwable {
        String providerName = joinPoint.getTarget().getClass().getSimpleName();
        String emailId = extractEmailId(joinPoint.getArgs());

        try {
            MDC.put(MDC_PROVIDER, providerName);
            MDC.put(MDC_EMAIL_ID, emailId);

            log.debug("PROVIDER: send | start | provider={} | emailId={}", providerName, emailId);

            Object result = joinPoint.proceed();

            log.info("PROVIDER: send | success | provider={} | emailId={}", providerName, emailId);
            return result;
        } catch (Exception e) {
            log.error("PROVIDER: send | failed | provider={} | emailId={} | reason={}",
                    providerName, emailId, SecurityMaskingUtils.rootMessage(e));
            throw e;
        } finally {
            MDC.remove(MDC_PROVIDER);
            MDC.remove(MDC_EMAIL_ID);
        }
    }

    @Around("providerAvailabilityCheck()")
    public Object logProviderAvailability(ProceedingJoinPoint joinPoint) throws Throwable {
        String providerName = joinPoint.getTarget().getClass().getSimpleName();

        Object result = joinPoint.proceed();
        boolean isAvailable = (boolean) result;

        if (!isAvailable) {
            log.warn("PROVIDER: availability | unavailable | provider={}", providerName);
        }

        return result;
    }

    private String extractEmailId(Object[] args) {
        if (args == null || args.length == 0) {
            return "[UNKNOWN]";
        }
        Object firstArg = args[0];
        if (firstArg instanceof Email email && email.getId() != null) {
            return SecurityMaskingUtils.maskId(email.getId());
        }
        return "[NEW]";
    }
}
