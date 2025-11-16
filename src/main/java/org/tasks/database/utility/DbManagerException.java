package org.tasks.database.utility;

import org.tasks.DatabaseException;

public class DbManagerException extends DatabaseException {
    DbManagerException(Throwable throwable) {
        super(throwable);
    }
}
