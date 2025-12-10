package org.tasks.service.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * CryptService calculates a password hash to store it at the database and validates if a password
 * entered by an authorizing user is compliant with the stored hash.
 */
@Service
public class CryptService {

    /**
     * Method generates salt and calculates a password hash with service algorithm information
     * @param pwd entered password
     * @return password hash
     */
    public String getHashedPassword(String pwd) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(pwd, salt);
    }

    /**
     * Method validates if an entered password is compliant with a previously calculated hash
     * @param pwd entered password
     * @param hash password hash
     * @return true - if password is compliant with hash, false - otherwise
     */
    public boolean checkPassword(String pwd, String hash) {
        return BCrypt.checkpw(pwd, hash);
    }
}
