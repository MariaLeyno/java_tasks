package org.tasks.database;

import com.google.common.collect.Multimap;
import org.tasks.DatabaseException;
import org.tasks.UserAccess;
import org.tasks.database.utility.DbManagerException;
import org.tasks.model.User;
import org.tasks.model.UserField;

import java.util.*;

public class UserRepository extends Repository<UserField> {
    private static final String USER_TABLE = "user_accounts";
    private static final String USER_SEQUENCE = "user_account_seq";
    private static final Map<String, UserAccess> accessMap = new HashMap<>();

    static {
        for (UserAccess access : UserAccess.values()) {
            accessMap.put(access.name(), access);
        }
    }

    public UserRepository() throws DatabaseException {
        super(USER_TABLE, USER_SEQUENCE, UserField.LOGIN);
    }

    public int deleteUsers(Set<String> logins) throws DbManagerException {
        return super.deleteDataObjects(logins);
    }

    public int updateUsers(Set<String> logins, Map<UserField, String> parameters) throws DatabaseException {
        return super.updateDataObjects(logins, parameters);
    }

    public int addNewUser(User user) throws DatabaseException {
        Map<UserField, String> userParameters = new TreeMap<>();
        userParameters.put(UserField.LOGIN, user.getLogin());
        userParameters.put(UserField.PASSWORD, user.getPassword());
        userParameters.put(UserField.ACCESS, user.getAccess().name());

        return super.addNewDataObject(userParameters);
    }

    public List<User> findUsersByParameters(Multimap<String, String> parameters) throws DatabaseException {
        List<Map<UserField, String>> result = findByParameters(parameters);
        List<User> users = new ArrayList<>();
        for (Map<UserField, String> objectMap : result) {
            String login = objectMap.get(UserField.LOGIN);
            String password = objectMap.get(UserField.PASSWORD);
            String access = objectMap.get(UserField.ACCESS);

            users.add(new User(login, password, getAccess(access)));
        }
        return users;
    }

    public Set<String> findLoginsByParameters(Multimap<String, String> parameters) throws DatabaseException {
        return findIdsByParameters(parameters);
    }

    private UserAccess getAccess(String strAccess) {
        if (strAccess == null || !accessMap.containsKey(strAccess.toUpperCase())) {
            return UserAccess.READ;
        }
        return accessMap.get(strAccess.toUpperCase());
    }
}
