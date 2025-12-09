package org.tasks.errors.db;

import org.tasks.errors.DatabaseException;

public class QueryBuildingException extends DatabaseException {
    public QueryBuildingException(String message) {
        super(message);
    }
}
