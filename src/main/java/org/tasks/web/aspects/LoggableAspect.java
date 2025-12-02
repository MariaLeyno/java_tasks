package org.tasks.web.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggableAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggableAspect.class);

    @Pointcut("within(@org.tasks.web.annotations.Loggable *) && execution(* * (..))")
    public void annotateHttpMethodsByLoggable() {}

    @Around("annotateHttpMethodsByLoggable()")
    public Object logToFile(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        logger.info("Method {} execution starts.", signature);
        System.out.printf("Method %s execution starts.%n", signature);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        logger.info("Method {} execution finished. Execution time: {} ms.", signature, duration);
        System.out.printf("Method %s execution finished. Execution time: %d ms.%n", signature, duration);
        return result;
    }
}
