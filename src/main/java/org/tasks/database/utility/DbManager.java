package org.tasks.database.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tasks.errors.db.DbManagerException;
import org.tasks.model.DataObjectField;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * DbManager manages connections to PostgreSql database. DbManager offers several methods to execute queries
 * at the database. In case of SQL or connectivity errors it throws {@link DbManagerException}.
 */
@Component
public final class DbManager {
    private final DataSource dataSource;

    @Autowired
    public DbManager(DataSource dataSource) {
        this.dataSource = dataSource;
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
     * Method creates a connection to the database.
     * @return connection to the database
     * @throws SQLException - if connectivity errors occur
     */
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}