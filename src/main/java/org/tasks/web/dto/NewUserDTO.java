package org.tasks.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record NewUserDTO(
        @NotEmpty
        @Size(min = 4, message = "User name should be at least 4 symbols")
        String login,
        @NotEmpty
        @Size(min = 6, message = "Password should be at least 6 symbols")
        String password,
        @NotEmpty
        String passwordAgain
) {}