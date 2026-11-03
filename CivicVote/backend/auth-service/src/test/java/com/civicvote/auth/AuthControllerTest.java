package com.civicvote.auth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
class AuthControllerTest {
    @Test void unknownOrIncorrectCredentialsReturnHelpfulUnauthorizedResponse() {
        UserRepository users=mock(UserRepository.class);
        AuthenticationManager manager=mock(AuthenticationManager.class);
        org.springframework.security.crypto.password.PasswordEncoder encoder=mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        JwtService jwt=new JwtService("unit-test-secret-that-is-at-least-32-bytes-long");
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        AuthController controller=new AuthController(users,encoder,manager,jwt);

        ResponseEntity<Map<String,Object>> response=controller.login(new AuthController.Credentials("ujwal","password-123"));

        assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
        assertEquals("Invalid username or password. Create a voter account if you have not registered yet.",response.getBody().get("message"));
        verifyNoInteractions(users);
    }
}
