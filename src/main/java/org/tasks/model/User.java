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
@ToString
public class User implements DataObject {
    private String login;
    private String password;
    private UserAccess access;

    @Override
    @JsonIgnore
    public String getPrimary() {
        return login;
    }
}
