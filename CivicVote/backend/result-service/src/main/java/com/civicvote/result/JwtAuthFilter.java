package com.civicvote.result;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final SecretKey key;
    public JwtAuthFilter(@Value("${security.jwt.secret}")String secret){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
    @Override protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain chain)throws ServletException,IOException{
        String header=request.getHeader("Authorization");
        if(header!=null&&header.startsWith("Bearer "))try{
            var claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            var auth=new UsernamePasswordAuthenticationToken(claims.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_"+claims.get("role",String.class))));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }catch(io.jsonwebtoken.JwtException|IllegalArgumentException ex){response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid or expired token");return;}
        chain.doFilter(request,response);
    }
}
