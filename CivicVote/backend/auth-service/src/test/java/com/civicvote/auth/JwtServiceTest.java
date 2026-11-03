package com.civicvote.auth;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.test.util.ReflectionTestUtils;
class JwtServiceTest {
    @Test void generatedTokenContainsIdentityAndRole() {
        JwtService service=new JwtService("unit-test-secret-that-is-at-least-32-bytes-long");
        UserAccount account=new UserAccount("voter", "hash", "VOTER");
        ReflectionTestUtils.setField(account,"id",7L);
        String token=service.create(account);
        var claims=Jwts.parser().verifyWith(Keys.hmacShaKeyFor("unit-test-secret-that-is-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(token).getPayload();
        assertEquals("voter",claims.get("username")); assertEquals("VOTER",claims.get("role")); assertNotNull(claims.getExpiration());
    }
}
