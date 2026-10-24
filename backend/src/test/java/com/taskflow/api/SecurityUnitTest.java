package com.taskflow.api;

import com.taskflow.api.model.User;
import com.taskflow.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUnitTest {
    @Test void jwtContainsTheAuthenticatedEmailAndExpires(){
        var jwt=new JwtService("test-secret-that-is-at-least-32-bytes-long-123456789",60_000);
        var user=new User("member@example.test","Test Member","test-hash");
        String token=jwt.issue(user);
        assertEquals("member@example.test",jwt.subject(token));
        assertTrue(jwt.expiration(token).isAfter(java.time.Instant.now()));
        assertNotNull(token);
    }
    @Test void bcryptVerifiesPasswordsWithoutStoringPlaintext(){
        var encoder=new BCryptPasswordEncoder(4);
        String hash=encoder.encode("a-long-test-password");
        assertNotEquals("a-long-test-password",hash);
        assertTrue(encoder.matches("a-long-test-password",hash));
        assertFalse(encoder.matches("wrong-password",hash));
    }
}
