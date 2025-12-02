package org.tasks.database.utility;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.tasks.errors.db.DbManagerException;
import org.tasks.model.ItemField;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.tasks.model.ItemField.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@Disabled
public class DbManagerTest {
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    private static DbManager dbManager;
    private Set<ItemField> fields = Set.of(ItemField.values());

    @BeforeAll
    static void init() {
        postgres.start();

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(postgres.getJdbcUrl());
        dataSource.setDatabaseName(postgres.getDatabaseName());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        dbManager = new DbManager(dataSource);
    }

    @DisplayName("01. ExecuteWithResult() should return a map with field values after selection by query with filters")
    @Test
    public void testExecuteWithResult_withFilter() throws DbManagerException {
        String query = "select ID, CATEGORY, BRAND, NAME, PRICE from catalog_items where CATEGORY = 'Dress'";

        List<Map<ItemField, String>> result = dbManager.executeWithResult(query, fields);

        assertThat(result).hasSize(2);
        Map<ItemField, String> firstRow = result.get(0);
        assertThat(firstRow).hasSize(5)
                .containsEntry(CATEGORY, "Dress").containsEntry(NAME, "Blue dress")
                .containsEntry(PRICE, "101.89").containsEntry(ID, "1").containsEntry(BRAND, "Zolla");
        Map<ItemField, String> lastRow = result.get(1);
        assertThat(lastRow).hasSize(5)
                .containsEntry(CATEGORY, "Dress").containsEntry(NAME, "Cocktail dress")
                .containsEntry(PRICE, "250").containsEntry(ID, "3").containsEntry(BRAND, "HM");
    }

    @DisplayName("02. ExecuteWithResult() should return a map with field values after selection by query without filters")
    @Test
    public void testExecuteWithResult_withoutFilter() throws DbManagerException {
        String query = "select ID, CATEGORY, BRAND, NAME, PRICE from catalog_items";

        List<Map<ItemField, String>> result = dbManager.executeWithResult(query, fields);

        assertThat(result).hasSize(3);
    }

    @DisplayName("03. ExecuteWithResult() should return an empty map if no rows were found")
    @Test
    public void testExecuteWithResult_withEmptyResult() throws DbManagerException {
        String query = "select ID, CATEGORY, BRAND, NAME, PRICE from catalog_items where PRICE > 300";

        List<Map<ItemField, String>> result = dbManager.executeWithResult(query, fields);

        assertThat(result).isEmpty();
    }

    @DisplayName("04. ExecuteWithResult() should throw an exception in case of query errors")
    @Test
    public void testExecuteWithResult_withQueryError() {
        String query = "select ID from catalog_item where CATEGORY = 'Dress'";

        DbManagerException exception
                = assertThrowsExactly(DbManagerException.class, () -> dbManager.executeWithResult(query, fields));

        assertThat(exception.getMessage()).contains("relation \"catalog_item\" does not exist");
    }

    @DisplayName("05. ExecuteWithParameters() should insert into the table a new row with specified column values")
    @ParameterizedTest
    @CsvSource(value = {"Socks, Ostin, Red socks, 10.1", "Socks, Ostin, Green socks, null"}, nullValues = "null")
    public void testExecuteWithParameters(String category, String brand, String name, String price) throws DbManagerException {
        String query = "insert into catalog_items (ID, CATEGORY, BRAND, NAME, PRICE) values (nextval('catalog_items_seq'), ?, ?, ?, ?)";
        Map<ItemField, String> parameters = new TreeMap<>();
        parameters.put(CATEGORY, category);
        parameters.put(BRAND, brand);
        parameters.put(NAME, name);
        parameters.put(PRICE, price);

        int result = dbManager.executeWithParameters(query, parameters);

        assertEquals(1, result);
    }

    @DisplayName("06. ExecuteWithParameters() should throw an exception if null-value is inserted into a non-null column")
    @ParameterizedTest
    @CsvSource(value = {"null, Ostin, Red socks, 10.1", "Socks, null, Red socks, 10.1", "Socks, Ostin, null, 10.1"}, nullValues = "null")
    public void testExecuteWithParameters_notAllowedNullValues(String category, String brand, String name, String price) throws DbManagerException {
        String query = "insert into catalog_items (ID, CATEGORY, BRAND, NAME, PRICE) values (nextval('catalog_items_seq'), ?, ?, ?, ?)";
        Map<ItemField, String> parameters = new TreeMap<>();
        parameters.put(CATEGORY, category);
        parameters.put(BRAND, brand);
        parameters.put(NAME, name);
        parameters.put(PRICE, price);

        DbManagerException exception
                = assertThrowsExactly(DbManagerException.class, () -> dbManager.executeWithParameters(query, parameters));

        assertThat(exception.getMessage()).contains("violates not-null constraint");
    }

    @DisplayName("07. ExecuteWithParameters() should throw an exception if not enough parameters are specified")
    @Test
    public void testExecuteWithParameters_notEnoughParameters() {
        String query = "insert into catalog_items (ID, CATEGORY, BRAND, NAME, PRICE) values (nextval('catalog_items_seq'), ?, ?, ?, ?)";
        Map<ItemField, String> parameters = Map.of(CATEGORY, "Socks", BRAND, "Nike", NAME, "Sport socks");

        DbManagerException exception
                = assertThrowsExactly(DbManagerException.class, () -> dbManager.executeWithParameters(query, parameters));

        assertThat(exception.getMessage()).contains("No value specified for parameter 4");
    }

    @DisplayName("08. ExecuteWithParameters() should throw an exception if number column value can not be parsed as double")
    @Test
    public void testExecuteWithParameters_incorrectNumber() {
        String query = "insert into catalog_items (ID, CATEGORY, BRAND, NAME, PRICE) values (nextval('catalog_items_seq'), ?, ?, ?, ?)";
        Map<ItemField, String> parameters = Map.of(CATEGORY, "Socks", BRAND, "Nike", NAME, "Sport socks", PRICE, "xxx");

        DbManagerException exception
                = assertThrowsExactly(DbManagerException.class, () -> dbManager.executeWithParameters(query, parameters));

        assertThat(exception.getMessage()).contains("For input string: \"xxx\"");
    }

    @DisplayName("09. ExecuteWithCount() should update rows with the specified identifiers")
    @Test
    public void testExecuteWithCount_updateWithFilter() throws DbManagerException {
        String query = "update catalog_items set NAME = 'Dark-blue dress' where ID = 1";

        int result = dbManager.executeWithCount(query);

        assertEquals(1, result);
    }

    @DisplayName("10. ExecuteWithCount() should update all rows if no identifiers specified")
    @Test
    public void testExecuteWithCount_updateWithoutFilters() throws DbManagerException {
        String query = "update user_accounts set ACCESS = 'FULL'";

        int result = dbManager.executeWithCount(query);

        assertEquals(2, result);
    }

    @DisplayName("11. ExecuteWithCount() should throw an exception in case of query errors")
    @Test
    public void testExecuteWithCount_withQueryError() {
        String query = "update catalog_items  where ID = 1";

        DbManagerException exception = assertThrowsExactly(DbManagerException.class, () -> dbManager.executeWithCount(query));

        assertThat(exception.getMessage()).contains("syntax error at or near \"where\"");
    }

    @DisplayName("12. ExecuteWithCount() should delete rows with the specified identifiers")
    @Test
    public void testExecuteWithCount_deleteWithFilter() throws DbManagerException {
        String query = "delete from catalog_items where ID = 3";

        int result = dbManager.executeWithCount(query);

        assertEquals(1, result);
    }

    @DisplayName("13. ExecuteWithCount() should delete all rows if no identifiers specified")
    @Test
    public void testExecuteWithCount_deleteWithoutFilters() throws DbManagerException {
        String query = "delete from user_accounts";

        int result = dbManager.executeWithCount(query);

        assertEquals(2, result);
    }

    @AfterAll
    static void finish() {
        postgres.stop();
    }
}
