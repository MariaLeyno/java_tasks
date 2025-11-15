package org.tasks.storage;

import org.tasks.model.User;

public class UserStorage extends Storage<User> {
    private static final String INFO = "%d users were loaded from the file";
    private static final String USERS_FILE = "auth/users";

    UserStorage() {
        super(USERS_FILE, User.class);
    }
}
