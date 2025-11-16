package org.tasks.database.utility;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.tasks.model.DataObjectField;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.tasks.database.utility.DbParameters.*;

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

    public static DbManager getInstance() throws DbManagerException {
        if (INSTANCE == null) {
            INSTANCE = instantiateDbManager();
        }
        return INSTANCE;
    }

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
        } catch (SQLException ex) {
            throw new DbManagerException(ex);
        }
    }

    public int executeWithCount(String query) throws DbManagerException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        } catch (SQLException ex) {
            throw new DbManagerException(ex);
        }
    }

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

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER_NAME, PASSWORD);
    }

    private static DbManager instantiateDbManager() throws DbManagerException {
        try (FileInputStream postgresEnv = new FileInputStream("postgresql.env");
                FileInputStream appProperties = new FileInputStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(postgresEnv);
            properties.load(appProperties);
            extractParametersValues(properties);

            String dbName = POSTGRES_DB.getValue();
            String dbHost = DB_HOST.getValue();
            String dbPort = DB_PORT.getValue();
            URL = String.format(URL_PATTERN, dbHost, dbPort, dbName);
            USER_NAME = POSTGRES_USER.getValue();
            PASSWORD = POSTGRES_PASSWORD.getValue();

            DEFAULT_SCHEMA = DEFAULT_DB_SCHEMA.getValue();
            LIQUIBASE_SCHEMA = LIQUIBASE_DB_SCHEMA.getValue();
            LIQUIBASE_CHANGELOG = LIQUIBASE_CHANGELOG_FILE.getValue();
        } catch (IOException ex) {
            throw new DbManagerException(ex);
        }

        DbManager dbManager = new DbManager();
        dbManager.createSchemaIfNotExist(LIQUIBASE_SCHEMA);
        dbManager.createSchemaIfNotExist(DEFAULT_SCHEMA);
        dbManager.processLiquibaseScripts();

        return dbManager;
    }

    private static void extractParametersValues(Properties properties) {
        for (DbParameters param : DbParameters.values()) {
            String value = properties.getProperty(param.name());
            param.setValue(value);
        }
    }
}
