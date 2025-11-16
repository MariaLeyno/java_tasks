package org.tasks.database.utility;

import lombok.Getter;
import lombok.Setter;

public enum DbParameters {
    POSTGRES_DB,
    POSTGRES_USER,
    POSTGRES_PASSWORD,
    DB_HOST,
    DB_PORT,
    DEFAULT_DB_SCHEMA,
    LIQUIBASE_DB_SCHEMA,
    LIQUIBASE_CHANGELOG_FILE;

    @Getter
    @Setter
    private String value;
}
