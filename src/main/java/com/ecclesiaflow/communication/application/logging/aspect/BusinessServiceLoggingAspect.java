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
 * Logging aspect for communication service operations.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class BusinessServiceLoggingAspect {

    private static final String MDC_EMAIL_ID = "emailId";

    @Pointcut("execution(* com.ecclesiaflow.communication.business.services.EmailService.sendEmail(..))")
    public void sendEmailOperation() {}

    @Pointcut("execution(* com.ecclesiaflow.communication.business.services.EmailService.queueEmail(..))")
    public void queueEmailOperation() {}

    @Pointcut("execution(* com.ecclesiaflow.communication.business.services.EmailService.processQueuedEmails(..))")
    public void processQueuedEmailsOperation() {}

    @Pointcut("execution(* com.ecclesiaflow.communication.business.services.EmailService.retryFailedEmails(..))")
    public void retryFailedEmailsOperation() {}

    @Around("sendEmailOperation()")
    public Object logSendEmail(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String emailId = extractEmailId(args);

        try {
            MDC.put(MDC_EMAIL_ID, emailId);
            log.info("SERVICE: sendEmail | start | emailId={}", emailId);

            Object result = joinPoint.proceed();

            log.info("SERVICE: sendEmail | success | emailId={}", emailId);
            return result;
        } catch (Exception e) {
            log.error("SERVICE: sendEmail | failed | emailId={} | reason={}",
                    emailId, SecurityMaskingUtils.rootMessage(e));
            throw e;
        } finally {
            MDC.remove(MDC_EMAIL_ID);
        }
    }

    @Around("queueEmailOperation()")
    public Object logQueueEmail(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String emailId = extractEmailId(args);

        try {
            MDC.put(MDC_EMAIL_ID, emailId);
            log.debug("SERVICE: queueEmail | start | emailId={}", emailId);

            Object result = joinPoint.proceed();

            log.debug("SERVICE: queueEmail | success | emailId={}", emailId);
            return result;
        } finally {
            MDC.remove(MDC_EMAIL_ID);
        }
    }

    @Around("processQueuedEmailsOperation()")
    public Object logProcessQueuedEmails(ProceedingJoinPoint joinPoint) throws Throwable {
        int batchSize = (int) joinPoint.getArgs()[0];
        log.info("SERVICE: processQueuedEmails | start | batchSize={}", batchSize);

        try {
            int successCount = (int) joinPoint.proceed();
            log.info("SERVICE: processQueuedEmails | success | processed={}", successCount);
            return successCount;
        } catch (Exception e) {
            log.error("SERVICE: processQueuedEmails | failed | reason={}",
                    SecurityMaskingUtils.rootMessage(e));
            throw e;
        }
    }

    @Around("retryFailedEmailsOperation()")
    public Object logRetryFailedEmails(ProceedingJoinPoint joinPoint) throws Throwable {
        int maxRetryCount = (int) joinPoint.getArgs()[0];
        int batchSize = (int) joinPoint.getArgs()[1];
        log.info("SERVICE: retryFailedEmails | start | maxRetry={} | batchSize={}",
                maxRetryCount, batchSize);

        try {
            int successCount = (int) joinPoint.proceed();
            log.info("SERVICE: retryFailedEmails | success | retried={}", successCount);
            return successCount;
        } catch (Exception e) {
            log.error("SERVICE: retryFailedEmails | failed | reason={}",
                    SecurityMaskingUtils.rootMessage(e));
            throw e;
        }
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

