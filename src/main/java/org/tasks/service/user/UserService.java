package org.tasks.service.user;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tasks.errors.DatabaseException;
import org.tasks.model.UserAccess;
import org.tasks.errors.UserException;
import org.tasks.database.UserRepository;
import org.tasks.errors.user.*;
import org.tasks.model.User;
import org.tasks.model.UserField;
import org.tasks.service.security.CryptService;
import org.tasks.service.security.TokenService;
import org.tasks.web.dto.NewUserDTO;

import java.util.List;

/**
 * UserService provides business logic for managing user's accounts. It validates data from UI and requests
 * the internal storage to extract and manipulate accounts parameters and states. Then it returns results to UI.
 * In case of validation or internal errors the service throws {@link UserException}.
 */
@Service
public class UserService {

    /** Storage for user accounts */
    private final UserRepository userRepository;
    /** Service to hash and check passwords */
    private final CryptService cryptService;
    /** Service to generate and validate authorization tokens */
    private final TokenService tokenService;

    @Autowired
    public UserService(UserRepository userRepository, CryptService cryptService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.cryptService = cryptService;
        this.tokenService = tokenService;
    }

    /**
     * Method validates parameters for a new user account and, if there is no errors, requests the internal storage
     * to store the new account.
     * @param userDTO user account parameters
     * @throws UserException - if account parameters are invalid or account storing failed
     */
    public void registerNewUser(NewUserDTO userDTO) throws UserException {
        String login = userDTO.login();
        String password = userDTO.password();

        validateUserName(login);
        validatePassword(password, userDTO.passwordAgain());

        if (findUserByLogin(login) != null) {
            throw new UserAlreadyExistsException(login);
        }

        String hashedPassword = cryptService.getHashedPassword(password);

        User newUser = new User(login, hashedPassword, UserAccess.CHANGE);
        saveUser(newUser);
    }

    /**
     * Method requests the internal storage to find an existing account by its parameters. If the account is found,
     * method validates that the password matches and return the authorization token.
     * @param login user login
     * @param password user password
     * @return authentication token
     * @throws UserException - if account is not found or password is invalid
     */
    public String authenticateUser(String login, String password) throws UserException {
        User user = findUserByLogin(login);
        if (user == null) {
            throw new UserNotFoundException(login);
        }

        if (!cryptService.checkPassword(password, user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        return tokenService.generateToken(user.getLogin(), user.getAccess());
    }

    /**
     * Method validates the account name for a new user account.
     * @param name user account name
     * @throws UserNameNotValidException - if name is empty or too short
     */
    private void validateUserName(String name) throws UserNameNotValidException {
        if (name == null || name.length() < 4) {
            throw new UserNameNotValidException();
        }
    }

    /**
     * Method validates the account password for a new user account.
     * @param password1 user account password
     * @param password2 user account password entered a second time
     * @throws PasswordNotValidException - if password is empty or twice entered passwords do not match
     */
    private void validatePassword(String password1, String password2) throws PasswordNotValidException {
        if (password1 == null || password1.isEmpty()) {
            throw new PasswordNotValidException();
        }
        if (!password1.equals(password2)) {
            throw new PasswordNotValidException("Passwords do not match");
        }
    }

    /**
     * Method requests the internal storage to store a new user account.
     * @param user new account
     * @throws UserNotSavedException - if storing failed
     */
    private void saveUser(User user) throws UserNotSavedException {
        try {
            userRepository.addNewUser(user);
        } catch (DatabaseException ex) {
            throw new UserNotSavedException(ex);
        }
    }

    /**
     * Method requests the internal storage to find an existing user account by its name.
     * @param login user account name
     * @return user account
     */
    private User findUserByLogin(String login) throws UserNotFoundException {
        Multimap<String, String> loginFilter = TreeMultimap.create();
        loginFilter.put(UserField.LOGIN.name(), login);
        try {
            List<User> found = userRepository.findUsersByParameters(loginFilter);
            return found == null || found.isEmpty() ? null : found.get(0);
        } catch (DatabaseException ex) {
            throw new UserNotFoundException(ex);
        }
    }
}
