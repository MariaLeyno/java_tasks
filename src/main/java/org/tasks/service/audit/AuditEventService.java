package org.tasks.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tasks.database.AuditEventRepository;
import org.tasks.errors.DatabaseException;
import org.tasks.errors.audit.AuditEventNotFoundException;
import org.tasks.errors.audit.AuditEventNotSavedException;
import org.tasks.errors.audit.InvalidEventParametersException;
import org.tasks.model.AuditEvent;
import org.tasks.model.AuditEventField;
import org.tasks.starter.audit.AuditException;
import org.tasks.starter.audit.AuditService;
import org.tasks.starter.audit.EventResult;
import org.tasks.starter.audit.EventType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditEventService implements AuditService {
    private static final Map<JoinPoint, Integer> EVENT_IDS = new HashMap<>();

    /** Storage for audit events */
    private final AuditEventRepository eventRepository;
    /** Service to map objects and json to strings and backwards */
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditEventService(AuditEventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void updateEvent(EventResult eventResult, JoinPoint joinPoint) throws AuditException {
        Integer eventId = EVENT_IDS.get(joinPoint);
        if (eventId == null) {
            throw new AuditEventNotFoundException("Event Id can not be found");
        }

        Map<AuditEventField, String> parametersToUpdate = new HashMap<>();
        parametersToUpdate.put(AuditEventField.END_TIME, Instant.now().toString());
        parametersToUpdate.put(AuditEventField.RESULT, eventResult.toString());

        try {
            eventRepository.updateEvent(eventId, parametersToUpdate);
        } catch (DatabaseException ex) {
            throw new AuditEventNotSavedException(ex);
        } finally {
            EVENT_IDS.remove(joinPoint, eventId);
        }
    }

    public void addNewEvent(EventType type, String userLogin, Map<String, Object> parameters, JoinPoint joinPoint)
            throws AuditException {
        try {
            String strParameters = objectMapper.writeValueAsString(parameters);
            AuditEvent auditEvent = new AuditEvent(type, userLogin, Instant.now(), strParameters);
            int eventId = eventRepository.addNewEvent(auditEvent);
            EVENT_IDS.put(joinPoint, eventId);
        } catch (JsonProcessingException ex) {
            throw new InvalidEventParametersException(ex);
        } catch (DatabaseException ex) {
            throw new AuditEventNotSavedException(ex);
        }
    }
}
