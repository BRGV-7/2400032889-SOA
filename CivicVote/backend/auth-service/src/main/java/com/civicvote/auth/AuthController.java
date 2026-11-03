package com.civicvote.auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager; private final JwtService jwt;
    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwt) { this.users=users; this.encoder=encoder; this.authenticationManager=authenticationManager; this.jwt=jwt; }
    public record Credentials(@NotBlank @Size(max=80) String username, @NotBlank @Size(min=8,max=100) String password) {}
    public record UserView(Long id,String username,String role) {}
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> register(@Valid @RequestBody Credentials input) {
        if(users.findByUsername(input.username()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Username is already registered");
        users.save(new UserAccount(input.username(),encoder.encode(input.password()),"VOTER"));
        return Map.of("message","Account created. Sign in to continue.");
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@Valid @RequestBody Credentials input) {
        try {
            Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.username(),input.password()));
            UserAccount user=users.findByUsername(authentication.getName()).orElseThrow();
            return ResponseEntity.ok(Map.of("token",jwt.create(user),"user",new UserView(user.getId(),user.getUsername(),user.getRole())));
        } catch(BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message","Invalid username or password. Create a voter account if you have not registered yet."));
        }
    }
    @GetMapping("/me")
    public UserView me(Authentication authentication) {
        UserAccount user=users.findByUsername(authentication.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new UserView(user.getId(),user.getUsername(),user.getRole());
    }
}
