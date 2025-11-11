package org.tasks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.tasks.UserAccess;

/**
 * Data object to store parameters values and state for user account.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"active"})
public class User implements DataObject {

    /** Account name */
    private String login;
    /** Account password */
    private String password;
    /** Account rights */
    private UserAccess access;
    /** Account state */
    private boolean active = true;

    public User(String login, String password, UserAccess access) {
        this(login, password, access, true);
    }

    /**
     * Method returns account name as unique identifier for user account.
     * @return name as account unique identifier
     */
    @Override
    @JsonIgnore
    public String getPrimary() {
        return login;
    }

    /**
     * Method changes user account state to inactive.
     */
    @Override
    public void setInactive() {
        active = false;
    }
}
