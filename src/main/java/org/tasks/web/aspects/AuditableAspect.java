package org.tasks.web.aspects;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tasks.errors.AuditEventException;
import org.tasks.model.EventResult;
import org.tasks.model.EventType;
import org.tasks.service.audit.AuditEventService;
import org.tasks.web.annotations.Auditable;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditableAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuditableAspect.class);

    private final AuditEventService auditEventService;

    @Autowired
    public AuditableAspect(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Pointcut("@annotation(org.tasks.web.annotations.Auditable)")
    public void annotateByAuditableHttpMethods() {}

    @Before("annotateByAuditableHttpMethods()")
    public void beforeMethod(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        Class[] parameterTypes = methodSignature.getParameterTypes();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> eventParameters = new HashMap<>();
        String user = null;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if (parameterTypes[i] == HttpServletRequest.class) {
                user = ((HttpServletRequest) arg).getHeader("user");
            } else {
                eventParameters.put(parameterTypes[i].getSimpleName() + "_" + i, arg);
            }
        }

        Auditable auditable = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Auditable.class);

        addEvent(auditable.eventType(), user, eventParameters, joinPoint);
    }

    @AfterReturning("annotateByAuditableHttpMethods()")
    public void afterMethodReturning(JoinPoint joinPoint) throws Throwable {
        updateEventWithResult(EventResult.SUCCESS, joinPoint);
    }

    @AfterThrowing(
            pointcut = "annotateByAuditableHttpMethods()",
            throwing = "ex"
    )
    public void afterMethodThrowable(JoinPoint joinPoint, Exception ex) throws Throwable {
        EventResult result = EventResult.ERROR;
        result.setMessage(ex.getMessage());
        updateEventWithResult(result, joinPoint);
    }

    private void addEvent(EventType type, String userLogin, Map<String, Object> parameters, JoinPoint joinPoint)
            throws AuditEventException {
        System.out.printf("Adding audit event: type = %s, userLogin = %s, parameters = %s, joinPoint = %s.%n",
                type, userLogin, parameters, joinPoint.getSignature());
        auditEventService.addNewEvent(type, userLogin, parameters, joinPoint);
    }

    private void updateEventWithResult(EventResult eventResult, JoinPoint joinPoint) throws AuditEventException {
        System.out.printf("Updating audit event: result = %s, joinPoint = %s.%n", eventResult, joinPoint.getSignature());
        auditEventService.updateEvent(eventResult, joinPoint);
    }
}
