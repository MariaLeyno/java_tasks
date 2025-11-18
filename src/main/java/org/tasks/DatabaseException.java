package org.tasks;

public abstract class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Throwable throwable) {
        super(throwable);
    }
}
