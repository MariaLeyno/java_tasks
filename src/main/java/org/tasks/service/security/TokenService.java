package org.tasks.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import org.tasks.model.UserAccess;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {
    private static final String ISSUER = "token-service";
    private static final String AUDIENCE = "marketplace";
    private static final String ACCESS_CLAIM = "access";

    private static final String SECRET = getSecret();

    public String generateToken(String login, UserAccess access) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        Instant currentTime = Instant.now();
        Instant expiredTime = currentTime.plus(5, ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(login)
                .withClaim(ACCESS_CLAIM, access.name())
                .withIssuedAt(currentTime)
                .withExpiresAt(expiredTime)
                .sign(algorithm);
    }

    public boolean validateToken(String token, UserAccess requiredAccess) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            if (!ISSUER.equals(decodedJWT.getIssuer())) {
                return false;
            }
            if (!decodedJWT.getAudience().contains(AUDIENCE)) {
                return false;
            }
            String subject = decodedJWT.getSubject();
            if (subject == null || subject.isEmpty()) {
                return false;
            }
            UserAccess access = decodedJWT.getClaim(ACCESS_CLAIM).as(UserAccess.class);
            if (access.compareTo(requiredAccess) < 0) {
                return false;
            }
        } catch (JWTVerificationException ex) {
            return false;
        }

        return true;
    }

    private static String getSecret() {
        String randomUUId = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(randomUUId.getBytes());
    }
}
