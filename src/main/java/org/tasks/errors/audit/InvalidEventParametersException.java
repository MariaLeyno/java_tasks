package org.tasks.errors.audit;

import org.tasks.starter.audit.AuditException;

public class InvalidEventParametersException extends AuditException {
    public InvalidEventParametersException(Throwable throwable) {
        super(throwable);
    }
}
