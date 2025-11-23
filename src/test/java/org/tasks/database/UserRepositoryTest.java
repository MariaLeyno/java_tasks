package org.tasks.database;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.tasks.DatabaseException;
import org.tasks.database.utility.DbManager;
import org.tasks.database.utility.DbManagerException;
import org.tasks.database.utility.DbParameters;
import org.tasks.model.DataObjectField;
import org.tasks.model.User;
import org.tasks.model.UserField;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.tasks.UserAccess.CHANGE;
import static org.tasks.UserAccess.READ;
import static org.tasks.model.UserField.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UserRepositoryTest {

    private static UserRepository userRepository;
    private static DbManager mockDbManager;

    private final String login = "user";
    private final String password = "passwrod";

    @BeforeAll
    public static void init() throws DatabaseException {
        DbParameters.DEFAULT_DB_SCHEMA.setValue("test");

        mockDbManager = mock(DbManager.class);
        try (MockedStatic<DbManager> mockedStatic = Mockito.mockStatic(DbManager.class)) {
            mockedStatic.when(DbManager::getInstance).thenReturn(mockDbManager);
            userRepository = new UserRepository();
        }
    }

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(mockDbManager);
    }

    @DisplayName("01. User parameters should be passed to database with INSERT query")
    @Test
    public void testAddNewUser() throws DatabaseException {
        when(mockDbManager.executeWithParameters(anyString(), anyMap())).thenReturn(1);

        int count = userRepository.addNewUser(new User(login, password, CHANGE));
        assertEquals(1, count);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<DataObjectField, String>> parametersCapture = ArgumentCaptor.forClass(Map.class);
        verify(mockDbManager).executeWithParameters(queryCapture.capture(), parametersCapture.capture());

        String query = queryCapture.getValue();
        Map<DataObjectField, String> parameters = parametersCapture.getValue();
        assertEquals("insert into test.user_accounts (ID, LOGIN, PASSWORD, ACCESS) values (nextval('test.user_account_seq'), ?, ?, ?)", query);
        assertThat(parameters).containsEntry(LOGIN, login);
        assertThat(parameters).containsEntry(PASSWORD, password);
        assertThat(parameters).containsEntry(ACCESS, CHANGE.name());
        assertThat(parameters).hasSize(3);
    }

    @DisplayName("02. User object to store can not be null")
    @Test
    public void testAddNewUser_nullUser() throws DatabaseException {
        QueryBuildingException exception
                = assertThrowsExactly(QueryBuildingException.class, () -> userRepository.addNewUser(null));
        assertEquals("User object to store should not be null", exception.getMessage());
        verifyNoInteractions(mockDbManager);
    }

    @DisplayName("03. User can not be saved in case of database error")
    @Test
    public void testAddNewUser_databaseError() throws DatabaseException {
        when(mockDbManager.executeWithParameters(anyString(), anyMap())).thenThrow(DbManagerException.class);

        assertThrowsExactly(DbManagerException.class, () -> userRepository.addNewUser(new User(login, password, CHANGE)));
    }

    @DisplayName("04. User data to update should be passed to database into UPDATE query")
    @Test
    public void testUpdateUsers() throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenReturn(2);

        Set<String> logins = Set.of("user1", "user2");
        Map<UserField, String> parameters = Map.of(ACCESS, READ.name());
        int count = userRepository.updateUsers(logins, parameters);
        assertEquals(2, count);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithCount(queryCapture.capture());

        String query = queryCapture.getValue();
        assertEquals("update test.user_accounts set ACCESS='READ' where (LOGIN='user1' OR LOGIN='user2')", query);
    }

    @DisplayName("05. User data to update should not be null or not applicable")
    @ParameterizedTest
    @NullAndEmptySource
    public void testUpdateUsers_emptyUpdate(Map<UserField, String> parameters) throws DatabaseException {
        QueryBuildingException exception
                = assertThrowsExactly(QueryBuildingException.class, () -> userRepository.updateUsers(Set.of(), parameters));
        assertEquals("Parameters to update are absent or invalid", exception.getMessage());
        verifyNoInteractions(mockDbManager);
    }

    @DisplayName("06. User may be updated without filters")
    @ParameterizedTest
    @NullAndEmptySource
    public void testUpdateUsers_emptyFilter(Set<String> logins) throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenReturn(12);

        Map<UserField, String> parameters = Map.of(ACCESS, READ.name());
        int count = userRepository.updateUsers(logins, parameters);
        assertEquals(12, count);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithCount(queryCapture.capture());

        String query = queryCapture.getValue();
        assertEquals("update test.user_accounts set ACCESS='READ'", query);
    }

    @DisplayName("07. Users can not be updated in case of database error")
    @Test
    public void testUpdateUsers_databaseError() throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenThrow(DbManagerException.class);

        assertThrowsExactly(DbManagerException.class, () -> userRepository.updateUsers(Set.of(), Map.of(ACCESS, READ.name())));
    }

    @DisplayName("08. Users to delete should be passed to database into DELETE query")
    @Test
    public void testDeleteUsers() throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenReturn(2);

        Set<String> logins = Set.of("user1", "user2");
        int count = userRepository.deleteUsers(logins);
        assertEquals(2, count);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithCount(queryCapture.capture());

        String query = queryCapture.getValue();
        assertEquals("delete from test.user_accounts where (LOGIN='user1' OR LOGIN='user2')", query);
    }

    @DisplayName("09. Users may be deleted without filters")
    @ParameterizedTest
    @NullAndEmptySource
    public void testDeleteUsers_emptyFilter(Set<String> logins) throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenReturn(12);

        int count = userRepository.deleteUsers(logins);
        assertEquals(12, count);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithCount(queryCapture.capture());

        String query = queryCapture.getValue();
        assertEquals("delete from test.user_accounts", query);
    }

    @DisplayName("10. Users can not be deleted in case of database error")
    @Test
    public void testDeleteUsers_databaseError() throws DatabaseException {
        when(mockDbManager.executeWithCount(anyString())).thenThrow(DbManagerException.class);

        assertThrowsExactly(DbManagerException.class, () -> userRepository.deleteUsers(Set.of()));
    }

    @DisplayName("11. Users can be found with field filters")
    @Test
    public void testFindUsersByParameters() throws DatabaseException {
        Map<DataObjectField, String> map1 = Map.of(LOGIN, "user1", PASSWORD, "password1", ACCESS, "READ");
        Map<DataObjectField, String> map2 = Map.of(LOGIN, "user2", PASSWORD, "password2", ACCESS, "READ");
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(List.of(map1, map2));

        Multimap<String, String> parameters = HashMultimap.create();
        parameters.put(ACCESS.name(), "READ");
        List<User> foundUsers = userRepository.findUsersByParameters(parameters);

        assertThat(foundUsers).hasSize(2);
        User firstUser = foundUsers.get(0);
        assertThat(firstUser.getLogin()).isEqualTo("user1");
        assertThat(firstUser.getPassword()).isEqualTo("password1");
        assertThat(firstUser.getAccess()).isEqualTo(READ);
        User secondUser = foundUsers.get(1);
        assertThat(secondUser.getLogin()).isEqualTo("user2");
        assertThat(secondUser.getPassword()).isEqualTo("password2");
        assertThat(secondUser.getAccess()).isEqualTo(READ);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select ID, LOGIN, PASSWORD, ACCESS from test.user_accounts where (ACCESS='READ')", query);
    }

    @DisplayName("12. Users can be found with no field filters specified")
    @ParameterizedTest
    @NullSource
    @ArgumentsSource(MultimapArgumentProvider.class)
    public void testFindUsersByParameters_withoutFilters(Multimap<String, String> parameters) throws DatabaseException {
        Map<DataObjectField, String> map1 = Map.of(LOGIN, "user1", PASSWORD, "password1", ACCESS, "READ");
        Map<DataObjectField, String> map2 = Map.of(LOGIN, "user2", PASSWORD, "password2", ACCESS, "CHANGE");
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(List.of(map1, map2));

        List<User> foundUsers = userRepository.findUsersByParameters(parameters);

        assertThat(foundUsers).hasSize(2);
        User firstUser = foundUsers.get(0);
        assertThat(firstUser.getLogin()).isEqualTo("user1");
        assertThat(firstUser.getPassword()).isEqualTo("password1");
        assertThat(firstUser.getAccess()).isEqualTo(READ);
        User secondUser = foundUsers.get(1);
        assertThat(secondUser.getLogin()).isEqualTo("user2");
        assertThat(secondUser.getPassword()).isEqualTo("password2");
        assertThat(secondUser.getAccess()).isEqualTo(CHANGE);

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select ID, LOGIN, PASSWORD, ACCESS from test.user_accounts", query);
    }

    @DisplayName("13. Users can be not found")
    @Test
    public void testFindUsersByParameters_emptyResult() throws DatabaseException {
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(Collections.emptyList());

        Multimap<String, String> parameters = HashMultimap.create();
        parameters.put(ACCESS.name(), "READ");
        List<User> foundUsers = userRepository.findUsersByParameters(parameters);

        assertThat(foundUsers).isEmpty();

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select ID, LOGIN, PASSWORD, ACCESS from test.user_accounts where (ACCESS='READ')", query);
    }

    @DisplayName("14. User logins can be found with field filters")
    @Test
    public void testFindUserLoginsByParameters() throws DatabaseException {
        Map<DataObjectField, String> map1 = Map.of(LOGIN, "user1");
        Map<DataObjectField, String> map2 = Map.of(LOGIN, "user2");
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(List.of(map1, map2));

        Multimap<String, String> parameters = HashMultimap.create();
        parameters.put(ACCESS.name(), "READ");
        Set<String> foundUsers = userRepository.findLoginsByParameters(parameters);

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).containsExactly("user1", "user2");

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select LOGIN from test.user_accounts where (ACCESS='READ')", query);
    }

    @DisplayName("15. User logins can be found with no field filters specified")
    @ParameterizedTest
    @NullSource
    @ArgumentsSource(MultimapArgumentProvider.class)
    public void testFindUserByParameters_withoutFilters(Multimap<String, String> parameters) throws DatabaseException {
        Map<DataObjectField, String> map1 = Map.of(LOGIN, "user1");
        Map<DataObjectField, String> map2 = Map.of(LOGIN, "user2");
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(List.of(map1, map2));

        Set<String> foundUsers = userRepository.findLoginsByParameters(parameters);

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).containsExactly("user1", "user2");

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select LOGIN from test.user_accounts", query);
    }

    @DisplayName("16. User logins can be not found")
    @Test
    public void testFindUserByParameters_emptyResult() throws DatabaseException {
        when(mockDbManager.executeWithResult(anyString(), anySet())).thenReturn(Collections.emptyList());

        Multimap<String, String> parameters = HashMultimap.create();
        parameters.put(ACCESS.name(), "READ");
        Set<String> foundUsers = userRepository.findLoginsByParameters(parameters);

        assertThat(foundUsers).isEmpty();

        ArgumentCaptor<String> queryCapture = ArgumentCaptor.forClass(String.class);
        verify(mockDbManager).executeWithResult(queryCapture.capture(), anySet());

        String query = queryCapture.getValue();
        assertEquals("select LOGIN from test.user_accounts where (ACCESS='READ')", query);
    }

    static class MultimapArgumentProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(HashMultimap.create())
            );
        }
    }
}
