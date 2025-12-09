package org.tasks.errors.audit;

import org.tasks.errors.AuditEventException;

public class AuditEventNotFoundException extends AuditEventException {
    public AuditEventNotFoundException(String message) {
        super(message);
    }
}
