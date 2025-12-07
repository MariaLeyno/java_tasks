package org.tasks.errors;

public class AuditEventException extends Exception {
    public AuditEventException(String message) {
        super(message);
    }

    public AuditEventException(Throwable throwable) {
        super(throwable);
    }
}
