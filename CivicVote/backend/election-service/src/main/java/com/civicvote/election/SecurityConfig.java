package com.civicvote.election;
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
            .authorizeHttpRequests(a->a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/elections/**").hasAnyRole("ADMIN","VOTER")
                .requestMatchers("/api/elections/**").hasRole("ADMIN").anyRequest().authenticated())
            .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
    }
}
