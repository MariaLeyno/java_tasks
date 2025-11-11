package org.tasks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.tasks.UserAccess;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"active"})
public class User implements DataObject {
    private String login;
    private String password;
    private UserAccess access;
    private boolean active = true;

    public User(String login, String password, UserAccess access) {
        this(login, password, access, true);
    }

    @Override
    @JsonIgnore
    public String getPrimary() {
        return login;
    }

    @Override
    public void setUnactive() {
        active = false;
    }
}
