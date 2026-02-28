package org.tasks;

import org.tasks.model.UserAccess;
import org.tasks.service.security.CryptService;

import java.util.Base64;

public class Test {
    public static void main(String[] args) {
        String password = "resu";

        String base64Pwd = Base64.getEncoder().encodeToString(password.getBytes());

        System.out.println("encoded: " + base64Pwd);

        CryptService cryptService = new CryptService();
        String hash = cryptService.getHashedPassword(base64Pwd);
        System.out.println("password = " + password + ", hash = " + hash);

        boolean check = cryptService.checkPassword(base64Pwd, hash);
        System.out.println("check = " + check);

        System.out.println(UserAccess.CHANGE.compareTo(UserAccess.FULL));
        System.out.println(UserAccess.CHANGE.compareTo(UserAccess.READ));
    }
}
