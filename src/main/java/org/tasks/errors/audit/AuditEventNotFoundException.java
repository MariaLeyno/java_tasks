package org.tasks.errors.audit;

import org.tasks.starter.audit.AuditException;

public class AuditEventNotFoundException extends AuditException {
    public AuditEventNotFoundException(String message) {
        super(message);
    }
}
