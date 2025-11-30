package org.tasks.errors.db;

import org.tasks.errors.DatabaseException;

public class DbManagerException extends DatabaseException {
    public DbManagerException(Throwable throwable) {
        super(throwable);
    }
}
