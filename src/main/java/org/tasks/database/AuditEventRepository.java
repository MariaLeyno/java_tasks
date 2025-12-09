package org.tasks.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tasks.errors.DatabaseException;
import org.tasks.model.AuditEvent;
import org.tasks.model.AuditEventField;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class AuditEventRepository extends Repository<AuditEventField> {
    private static final String EVENT_TABLE = "audit_events";
    private static final String EVENT_SEQUENCE = "audit_events_seq";

    AuditEventRepository(@Value("${spring.datasource.schema}") String schema) {
        super(schema, EVENT_TABLE, EVENT_SEQUENCE, AuditEventField.ID);
    }

    public int updateEvent(int id, Map<AuditEventField, String> parameters) throws DatabaseException {
        Set<String> ids = Set.of(String.valueOf(id));
        return super.updateDataObjects(ids, parameters);
    }

    public int addNewEvent(AuditEvent event) throws DatabaseException {
        Map<AuditEventField, String> eventParameters = new TreeMap<>();
        eventParameters.put(AuditEventField.TYPE, event.getType());
        eventParameters.put(AuditEventField.AUTHOR, event.getAuthor());
        eventParameters.put(AuditEventField.START_TIME, event.getStartTime());
        eventParameters.put(AuditEventField.PARAMETERS, event.getParameters());
        eventParameters.put(AuditEventField.END_TIME, event.getEndTime());
        eventParameters.put(AuditEventField.RESULT, event.getResult());

        return super.addNewDataObject(eventParameters);
    }
}
