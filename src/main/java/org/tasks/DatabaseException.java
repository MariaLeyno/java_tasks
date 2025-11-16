package org.tasks;

public abstract class DatabaseException extends Exception {
    public DatabaseException(Throwable throwable) {
        super(throwable);
    }
}
