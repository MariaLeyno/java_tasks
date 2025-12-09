package org.tasks.errors.audit;

import org.tasks.starter.audit.AuditException;

public class AuditEventNotSavedException extends AuditException {
    public AuditEventNotSavedException(Throwable throwable) {
        super(throwable);
    }
}
