package com.ecclesiaflow.communication.application.logging.aspect;

import com.ecclesiaflow.communication.application.logging.SecurityMaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Logging aspect for scheduled jobs.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class ScheduledJobLoggingAspect {

    @Pointcut("execution(* com.ecclesiaflow.communication.application.jobs.EmailQueueProcessor.processQueuedEmails(..))")
    public void processQueuedEmailsJob() {}

    @Pointcut("execution(* com.ecclesiaflow.communication.application.jobs.EmailQueueProcessor.retryFailedEmails(..))")
    public void retryFailedEmailsJob() {}

    @Around("processQueuedEmailsJob()")
    public Object logProcessQueue(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("SCHEDULER: processQueuedEmails | start");
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            int processedCount = result instanceof Integer i ? i : 0;
            if (processedCount > 0) {
                log.info("SCHEDULER: processQueuedEmails | success | processed={} | durationMs={}", processedCount, duration);
            } else {
                log.debug("SCHEDULER: processQueuedEmails | success | processed=0 | durationMs={}", duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("SCHEDULER: processQueuedEmails | failed | durationMs={} | reason={}",
                    duration, SecurityMaskingUtils.rootMessage(e));
            throw e;
        }
    }

    @Around("retryFailedEmailsJob()")
    public Object logRetryFailed(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("SCHEDULER: retryFailedEmails | start");
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            int retriedCount = result instanceof Integer i ? i : 0;
            if (retriedCount > 0) {
                log.info("SCHEDULER: retryFailedEmails | success | retried={} | durationMs={}", retriedCount, duration);
            } else {
                log.debug("SCHEDULER: retryFailedEmails | success | retried=0 | durationMs={}", duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("SCHEDULER: retryFailedEmails | failed | durationMs={} | reason={}",
                    duration, SecurityMaskingUtils.rootMessage(e));
            throw e;
        }
    }
}
