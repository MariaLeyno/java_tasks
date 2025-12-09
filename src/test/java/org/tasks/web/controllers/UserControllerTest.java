package org.tasks.web.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
import org.tasks.errors.UserException;
import org.tasks.errors.user.AuthenticationFailedException;
import org.tasks.errors.user.PasswordNotValidException;
import org.tasks.errors.user.UserAlreadyExistsException;
import org.tasks.errors.user.UserNameNotValidException;
import org.tasks.errors.user.UserNotFoundException;
import org.tasks.errors.user.UserNotSavedException;
import org.tasks.service.user.UserService;
import org.tasks.web.dto.NewUserDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UserControllerTest {

    private static MockMvc mockMvc;

    @BeforeAll
    public static void init() throws UserException {
        UserService userService = mock(UserService.class);
        doAnswer(invocation -> {
            NewUserDTO dto = invocation.getArgument(0);
            String login = dto.login();
            switch (login) {
                case "use" -> throw new UserNameNotValidException();
                case "existing_user" -> throw new UserAlreadyExistsException(login);
                case "unlucky_user" -> throw new UserNotSavedException(new RuntimeException());
            }
            if (!dto.password().equals(dto.passwordAgain())) {
                throw new PasswordNotValidException("Passwords do not match");
            }
            return null;
        }).when(userService).registerNewUser(any(NewUserDTO.class));
        when(userService.authenticateUser(eq("admin"), eq("1111"))).thenReturn("root_token_111");
        when(userService.authenticateUser(eq("not_existing_user"), anyString())).thenThrow(UserNotFoundException.class);
        when(userService.authenticateUser(eq("forgetful_user"), anyString())).thenThrow(AuthenticationFailedException.class);
        when(userService.authenticateUser(eq("unlucky_user"), anyString())).thenThrow(UserNotSavedException.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @DisplayName("01. New user with valid login and passwords should be registered.")
    @Test
    public void testRegisterUser_success() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "admin")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("New user's registration is successful"))
                .andReturn();
        assertNotNull(mvcResult);
    }

    @DisplayName("02. New user can not be registered without a login.")
    @Test
    public void testRegisterUser_lostLogin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException());
        assertThat(mvcResult.getResolvedException().getMessage()).contains("on field 'login'");
    }

    @DisplayName("03. New user can not be registered without a password.")
    @Test
    public void testRegisterUser_lostPassword() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "admin")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException());
        assertThat(mvcResult.getResolvedException().getMessage()).contains("on field 'password'");
    }

    @DisplayName("04. New user can not be registered without a password confirmation.")
    @Test
    public void testRegisterUser_lostPasswordConfirmation() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "admin")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException());
        assertThat(mvcResult.getResolvedException().getMessage()).contains("on field 'passwordAgain'");
    }

    @DisplayName("05. New user can not be registered with too short login.")
    @Test
    public void testRegisterUser_shortLogin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "use")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("06. New user can not be registered if password is not confirmed.")
    @Test
    public void testRegisterUser_notValidPasswords() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "admin")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "2222")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("07. New user can not be registered if service error occurs.")
    @Test
    public void testRegisterUser_alreadyExistingLogin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "existing_user")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("08. New user can not be registered with login of already existing user.")
    @Test
    public void testRegisterUser_internalError() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/user")
                                .queryParam("login", "unlucky_user")
                                .queryParam("password", "1111")
                                .queryParam("passwordAgain", "1111")
                )
                .andExpect(status().isInternalServerError())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("09. User with valid login and password should receive an authentication token.")
    @Test
    public void testAuthenticateUser_success() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("login", "admin")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isAccepted())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        assertNotNull(mvcResult);
    }

    @DisplayName("10. User authentication can not be processed without login.")
    @Test
    public void testAuthenticateUser_lostLogin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(MissingServletRequestParameterException.class, mvcResult.getResolvedException());
        assertThat(mvcResult.getResolvedException().getMessage()).contains("parameter 'login'");
    }

    @DisplayName("11. User authentication can not be processed without password.")
    @Test
    public void testAuthenticateUser_lostPassword() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("login", "admin")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(MissingServletRequestParameterException.class, mvcResult.getResolvedException());
        assertThat(mvcResult.getResolvedException().getMessage()).contains("parameter 'password'");
    }

    @DisplayName("12. User authentication fails if the user is not found.")
    @Test
    public void testAuthenticateUser_userNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("login", "not_existing_user")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isNotFound())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("13. User authentication fails if the password is incorrect.")
    @Test
    public void testAuthenticateUser_incorrectPassword() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("login", "forgetful_user")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }

    @DisplayName("14. User authentication fails if service error occurs.")
    @Test
    public void testAuthenticateUser_internalError() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                put("/user")
                                .queryParam("login", "unlucky_user")
                                .queryParam("password", "1111")
                )
                .andExpect(status().isInternalServerError())
                .andReturn();
        assertNotNull(mvcResult);
        assertInstanceOf(ResponseStatusException.class, mvcResult.getResolvedException());
    }
}
