package org.tasks.errors.audit;

import org.tasks.errors.AuditEventException;

public class AuditEventNotSavedException extends AuditEventException {
    public AuditEventNotSavedException(Throwable throwable) {
        super(throwable);
    }
}
