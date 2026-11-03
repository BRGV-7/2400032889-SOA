package com.civicvote.auth;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
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
@Configuration
public class SecurityConfig {
    static class JwtFilter extends OncePerRequestFilter {
        private final SecretKey key;
        JwtFilter(String secret){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
        @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
            String header=request.getHeader("Authorization");
            if(header!=null&&header.startsWith("Bearer "))try{
                var claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
                var auth=new UsernamePasswordAuthenticationToken(claims.get("username",String.class),null,List.of(new SimpleGrantedAuthority("ROLE_"+claims.get("role",String.class))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }catch(io.jsonwebtoken.JwtException|IllegalArgumentException ex){response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid or expired token");return;}
            chain.doFilter(request,response);
        }
    }
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean UserDetailsService userDetailsService(UserRepository users){return username->users.findByUsername(username).filter(user->user.isEnabled()).map(u->User.withUsername(u.getUsername()).password(u.getPasswordHash()).roles(u.getRole()).build()).orElseThrow(()->new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));}
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,@Value("${security.jwt.secret}")String secret)throws Exception{return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.requestMatchers("/api/auth/login","/api/auth/register","/actuator/health").permitAll().anyRequest().authenticated()).httpBasic(b->b.disable()).formLogin(f->f.disable()).addFilterBefore(new JwtFilter(secret),UsernamePasswordAuthenticationFilter.class).build();}
    @Bean ApplicationRunner developmentAdmin(UserRepository users,PasswordEncoder encoder,org.springframework.core.env.Environment env){
        return args->{String username=env.getProperty("ADMIN_USERNAME");String password=env.getProperty("ADMIN_PASSWORD");if(username!=null&&!username.isBlank()&&password!=null&&!password.isBlank())users.findByUsername(username).orElseGet(()->users.save(new UserAccount(username,encoder.encode(password),"ADMIN")));};
    }
}
