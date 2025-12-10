package org.tasks.errors;

public abstract class ItemException extends Exception {
    public ItemException(String message) {
        super(message);
    }

    public ItemException(Throwable throwable) {
        super(throwable);
    }
}
