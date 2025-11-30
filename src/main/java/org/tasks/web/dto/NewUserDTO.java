package org.tasks.web.dto;

import java.util.Objects;

public record NewUserDTO(String login, String password, String passwordAgain) {
    public NewUserDTO {
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);
        Objects.requireNonNull(passwordAgain);
    }
}
