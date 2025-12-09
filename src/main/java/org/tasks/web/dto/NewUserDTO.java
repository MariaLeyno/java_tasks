package org.tasks.web.dto;

import jakarta.validation.constraints.NotNull;

public record NewUserDTO(@NotNull String login, @NotNull String password, @NotNull String passwordAgain) { }