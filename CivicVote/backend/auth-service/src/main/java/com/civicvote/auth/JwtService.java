package com.civicvote.auth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
    private final SecretKey key;
    public JwtService(@Value("${security.jwt.secret}") String secret) { key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
    public String create(UserAccount user) { Instant now=Instant.now(); return Jwts.builder().subject(user.getId().toString()).claim("username",user.getUsername()).claim("role",user.getRole()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600))).signWith(key).compact(); }
}
