package org.tasks.database.utility;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.postgresql.Driver;
import org.tasks.model.DataObjectField;

import java.sql.*;
import java.util.*;

import static org.tasks.database.utility.DbParameters.*;

/**
 * DbManager manages connections to PostgreSql database. As a singleton there is the only DBManager through the application.
 * While starting, it loads connection parameters (i.e. database host and port) from Application properties files and
 * calls Liquibase to execute its changelog scripts.
 * DbManager offers several methods to execute queries at the database. In case of SQL or connectivity errors it throws
 * {@link DbManagerException}.
 */
public final class DbManager {
    private static final String URL_PATTERN = "jdbc:postgresql://%s:%s/%s";
    private static final String CREATE_SCHEMA_QUERY = "create schema if not exists %s";

    private static DbManager INSTANCE;

    private static String URL;
    private static String USER_NAME;
    private static String PASSWORD;

    private static String DEFAULT_SCHEMA;
    private static String LIQUIBASE_SCHEMA;
    private static String LIQUIBASE_CHANGELOG;

    /**
     * Method creates a singleton instance for DbManager and returns it for all requests.
     * In case of any errors {@link DbManagerException} is thrown and DbManager instance creation will be postponed
     * for the next request.
     * @return DbManager singleton instance
     * @throws DbManagerException - if connectivity, files reading or data migration errors occur
     */
    public static DbManager getInstance() throws DbManagerException {
        if (INSTANCE == null) {
            INSTANCE = instantiateDbManager();
        }
        return INSTANCE;
    }

    /**
     * Method executes SQL query at the database and returns a result as a list of rows with a map of columns and values.
     * @param query SQL query to execute
     * @param fields a set of fields to extract from a result
     * @return SQL query result as a list of rows with a map of columns and values
     * @param <T> Data Object fields
     * @throws DbManagerException - if connectivity or SQL errors occur
     */
    public <T extends DataObjectField> List<Map<T, String>> executeWithResult(String query, Set<T> fields)
            throws DbManagerException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            List<Map<T, String>> result = new ArrayList<>();
            while(resultSet.next()) {
                Map<T, String> entitytMap = new HashMap<>();
                for (T field : fields) {
                    entitytMap.put(field, resultSet.getString(field.toString()));
                }
                result.add(entitytMap);
            }
            return result;
        } catch (SQLException ex) {
            throw new DbManagerException(ex);
        }
    }

    /**
     * Methods injects string or number parameters into SQL query and executes it at the database.
     * @param query SQL query to execute
     * @param parameters map of data object fields and their values
     * @return a number of affected rows
     * @param <T> Data Object fields
     * @throws DbManagerException - if parameters injecting fails or connectivity or SQL errors occur
     */
    public <T extends DataObjectField> int executeWithParameters(String query, Map<T, String> parameters)
            throws DbManagerException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            int k = 1;
            for (Map.Entry<T, String> entry : parameters.entrySet()) {
                String value = entry.getValue();
                switch (entry.getKey().getType()) {
                    case STRING -> {
                        if(value == null) {
                            statement.setNull(k++, Types.VARCHAR);
                        } else {
                            statement.setString(k++, value);
                        }
                    }
                    case NUMBER -> {
                        if (value == null) {
                            statement.setNull(k++, Types.BIGINT);
                        } else {
                            statement.setDouble(k++, Double.parseDouble(value));
                        }
                    }
                }
            }
            return statement.executeUpdate();
        } catch (SQLException | IllegalArgumentException ex) {
            throw new DbManagerException(ex);
        }
    }

    /**
     * Method executes SQL query at the database.
     * @param query SQL query to execute
     * @return a number of affected rows
     * @throws DbManagerException - if connectivity or SQL errors occur
     */
    public int executeWithCount(String query) throws DbManagerException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        } catch (SQLException ex) {
            throw new DbManagerException(ex);
        }
    }

    /**
     * Method calls Liquibase data migration to separate database schemas if specified.
     * @throws DbManagerException - if connectivity or data migration errors occur
     */
    private void processLiquibaseScripts() throws DbManagerException {
        try (JdbcConnection jdbcConnection = new JdbcConnection(getConnection())) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            database.setDefaultSchemaName(DEFAULT_SCHEMA);
            database.setLiquibaseSchemaName(LIQUIBASE_SCHEMA);

            Liquibase liquibase = new Liquibase(LIQUIBASE_CHANGELOG, new ClassLoaderResourceAccessor(), database);
            liquibase.update();
        } catch (SQLException | LiquibaseException ex) {
            throw new DbManagerException(ex);
        }
    }

    /**
     * Method creates a database schema for entity tables and a separate schema for Liquibase service tables
     * if the schemas are specified at properties files and not already created at the database.
     * @param schema database schema to create
     * @throws DbManagerException - if connectivity or SQL errors occur
     */
    private void createSchemaIfNotExist(String schema) throws DbManagerException {
        if (schema == null || schema.isEmpty()) {
            return;
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            String query = String.format(CREATE_SCHEMA_QUERY, schema);
            int count = statement.executeUpdate(query);
        } catch (SQLException ex) {
            throw new DbManagerException(ex);
        }
    }

    /**
     * Method creates a connection to the database.
     * @return connection to the database
     * @throws SQLException - if connectivity errors occur
     */
    private Connection getConnection() throws SQLException {
        DriverManager.registerDriver(new Driver());
        return DriverManager.getConnection(URL, USER_NAME, PASSWORD);
    }

    /**
     * Method loads application properties from specified properties files and configures the database connection
     * parameters. Then it creates the only instance of DbManager and calls Liquibase data migration to the database.
     * @return DbManager instance
     * @throws DbManagerException - if files reading, connectivity or data migration errors occur
     */
    private static DbManager instantiateDbManager() throws DbManagerException {
        String dbName = POSTGRES_DB.getValue();
        String dbHost = DB_HOST.getValue();
        String dbPort = DB_PORT.getValue();
        URL = String.format(URL_PATTERN, dbHost, dbPort, dbName);
        USER_NAME = POSTGRES_USER.getValue();
        PASSWORD = POSTGRES_PASSWORD.getValue();

        DEFAULT_SCHEMA = DEFAULT_DB_SCHEMA.getValue();
        LIQUIBASE_SCHEMA = LIQUIBASE_DB_SCHEMA.getValue();
        LIQUIBASE_CHANGELOG = LIQUIBASE_CHANGELOG_FILE.getValue();

        DbManager dbManager = new DbManager();
        dbManager.createSchemaIfNotExist(LIQUIBASE_SCHEMA);
        dbManager.createSchemaIfNotExist(DEFAULT_SCHEMA);
        dbManager.processLiquibaseScripts();

        return dbManager;
    }
}
