package com.civicvote.voting;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {
    @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthFilter jwt)throws Exception{
        return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll().requestMatchers("/actuator/health").permitAll().requestMatchers("/api/votes/internal/**").permitAll().requestMatchers("/api/votes/**").hasRole("VOTER").anyRequest().authenticated())
            .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
    }
}
