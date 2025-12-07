package org.tasks.errors.audit;

import org.tasks.errors.AuditEventException;

public class InvalidEventParametersException extends AuditEventException {
    public InvalidEventParametersException(Throwable throwable) {
        super(throwable);
    }
}
