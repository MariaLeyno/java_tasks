package org.tasks.web.controllers;

import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
import org.tasks.errors.UserException;
import org.tasks.errors.user.AuthenticationFailedException;
import org.tasks.errors.user.UserNotFoundException;
import org.tasks.errors.user.UserNotSavedException;
import org.tasks.service.user.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class UserControllerTest {

    private static MockMvc mockMvc;

    @BeforeAll
    public static void init() throws UserException {
        UserService userService = mock(UserService.class);
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

    @DisplayName("1. User with valid login and password should receive an authentication token.")
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

    @DisplayName("2. User authentication can no be processed without login.")
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

    @DisplayName("3. User authentication can no be processed without password.")
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

    @DisplayName("4. User authentication fails if the user is not found.")
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

    @DisplayName("5. User authentication fails if the password is incorrect.")
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

    @DisplayName("6. User authentication fails if a service error occurs.")
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
