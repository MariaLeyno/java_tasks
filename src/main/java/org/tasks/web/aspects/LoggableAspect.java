package org.tasks.web.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggableAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggableAspect.class);

    @Pointcut("within(@org.tasks.web.servlet.annotations.Loggable *) && execution(* * (..))")
    public void annotateByLoggable() {}

    @Around("annotateByLoggable()")
    public Object logToFile(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        logger.info("Method {} execution starts.", signature);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        logger.info("Method {} execution finished. Execution time: {} ms.", signature, duration);
        return result;
    }
}
