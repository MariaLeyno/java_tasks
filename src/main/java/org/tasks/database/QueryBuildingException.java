package org.tasks.database;

import org.tasks.DatabaseException;

public class QueryBuildingException extends DatabaseException {
    QueryBuildingException(String message) {
        super(message);
    }
}
