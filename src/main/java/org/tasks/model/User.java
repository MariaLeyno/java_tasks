package org.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data object to store parameters values and state for user account.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements DataObject {

    /** Account unique identifier */
    private Integer id;
    /** Account name */
    private String login;
    /** Account password */
    private String password;
    /** Account rights */
    private UserAccess access;

    public User(String login, String password, UserAccess access) {
        this(null, login, password, access);
    }
}
