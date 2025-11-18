package org.tasks.database.utility;

import org.tasks.DatabaseException;

public class DbManagerException extends DatabaseException {
    public DbManagerException(Throwable throwable) {
        super(throwable);
    }
}
