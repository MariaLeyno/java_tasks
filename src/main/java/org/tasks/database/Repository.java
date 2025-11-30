package org.tasks.database;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tasks.errors.DatabaseException;
import org.tasks.database.filtering.FieldWithValues;
import org.tasks.database.filtering.NumberOperation;
import org.tasks.database.utility.DbManager;
import org.tasks.errors.db.DbManagerException;
import org.tasks.errors.db.QueryBuildingException;
import org.tasks.model.DataObjectField;
import org.tasks.model.FieldType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tasks.database.SqlConstants.*;

public abstract class Repository<E extends Enum<E> & DataObjectField> {
    private static final String SELECT = "select %s from %s";
    private static final String INSERT = "insert into %s (%s) values (nextval('%s'), %s)";
    private static final String UPDATE = "update %s set ";
    private static final String DELETE = "delete from %s";
    private static final String WHERE = " where ";

    private DbManager dbManager;

    @Value("${spring.liquibase.default-schema}")
    protected String schema;

    private final String table;
    private final String sequence;
    private final E primaryField;
    private final Map<String, E> fields = new HashMap<>();

    private final String selectPrimaryQuery;
    private final String selectQuery;
    private final String insertQuery;
    private final String updateQuery;
    private final String deleteQuery;

    Repository(String tableName, String sequenceName, E primaryField) {
        if (StringUtils.isEmpty(schema)) {
            this.table = tableName;
            this.sequence = sequenceName;
        } else {
            this.table = schema + "." + tableName;
            this.sequence = schema + "." + sequenceName;
        }
        this.primaryField = primaryField;

        for (E field : primaryField.getDeclaringClass().getEnumConstants()) {
            if (field.isToSave()) {
                fields.put(field.name(), field);
            }
        }

        String strFields = fields.values().stream().sorted().map(Enum::name).collect(Collectors.joining(COMMA));
        String strQuestions = IntStream.range(0, fields.size() - 1).mapToObj(i -> QUESTION_MARK)
                .collect(Collectors.joining(COMMA));

        this.selectPrimaryQuery = String.format(SELECT, primaryField, table);
        this.selectQuery = String.format(SELECT, strFields, table);
        this.insertQuery = String.format(INSERT, table, strFields, sequence, strQuestions);
        this.updateQuery = String.format(UPDATE, table);
        this.deleteQuery = String.format(DELETE, table);
    }

    @Autowired
    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    protected int deleteDataObjects(Set<String> ids) throws DbManagerException {
        String query = deleteQuery;

        if (ids != null && !ids.isEmpty()) {
            query = query + getStrPrimaryFilters(ids);
        }

        return dbManager.executeWithCount(query);
    }

    protected int updateDataObjects(Set<String> ids, Map<E, String> parametersToUpdate) throws DatabaseException {
        String strFieldsToUpdate = getStrFieldsToUpdate(parametersToUpdate);
        if (strFieldsToUpdate == null || strFieldsToUpdate.isEmpty()) {
            throw new QueryBuildingException("Parameters to update are absent or invalid");
        }
        String query = updateQuery + strFieldsToUpdate;

        if (ids != null && !ids.isEmpty()) {
            query = query + getStrPrimaryFilters(ids);
        }

        return dbManager.executeWithCount(query);
    }

    protected int addNewDataObject(Map<E, String> parameters) throws DatabaseException {
        return dbManager.executeWithParameters(insertQuery, parameters);
    }

    protected List<Map<E, String>> findByParameters(Multimap<String, String> parameters) throws DatabaseException {
        Set<FieldWithValues<E>> filters = getFieldsWithValues(parameters);
        String query = selectQuery + getStrFilters(filters);
        return dbManager.executeWithResult(query, new HashSet<>(fields.values()));
    }

    protected Set<String> findIdsByParameters(Multimap<String, String> parameters) throws DatabaseException {
        Set<FieldWithValues<E>> filters = getFieldsWithValues(parameters);
        String query = selectPrimaryQuery + getStrFilters(filters);
        return dbManager.executeWithResult(query, Set.of(primaryField)).stream()
                .flatMap(map -> map.values().stream()).collect(Collectors.toSet());
    }

    private String getStrFieldsToUpdate(Map<E, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        return parameters.entrySet().stream().map(entry -> {
            E field = entry.getKey();
            return switch (field.getType()) {
                case STRING -> field.name() + EQUAL + QUOTE + entry.getValue() + QUOTE;
                case NUMBER -> field.name() + EQUAL + entry.getValue();
                default -> null;
            };
        }).filter(Objects::nonNull).collect(Collectors.joining(COMMA));
    }

    private String getStrPrimaryFilters(Set<String> ids) {
        if (primaryField.getType() == FieldType.NUMBER) {
            ids = ids.stream().map(id -> NumberOperation.EQ.name() + id).collect(Collectors.toSet());
        }
        FieldWithValues<E> primaryFilter = new FieldWithValues<>(primaryField, ids);
        return getStrFilters(Set.of(primaryFilter));
    }

    private String getStrFilters(Set<FieldWithValues<E>> filters) {
        String strFilters = filters.stream()
                .map(filter -> String.format(BRACKETS, filter.getSqlRepresentation()))
                .collect(Collectors.joining(AND));
        return strFilters.isEmpty() ? strFilters : WHERE + strFilters;
    }

    private Set<FieldWithValues<E>> getFieldsWithValues(Multimap<String, String> parameters) {
        Set<FieldWithValues<E>> filters = new TreeSet<>();
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
                E field = fields.get(entry.getKey());
                FieldWithValues<E> filter = new FieldWithValues<>(field, entry.getValue());
                filters.add(filter);
            }
        }
        return filters;
    }
}