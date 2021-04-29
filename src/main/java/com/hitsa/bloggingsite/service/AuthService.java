package com.hitsa.bloggingsite.service;

import com.hitsa.bloggingsite.dto.AuthenticationResponse;
import com.hitsa.bloggingsite.dto.LoginRequest;
import com.hitsa.bloggingsite.dto.RefreshTokenRequest;
import com.hitsa.bloggingsite.dto.RegisterRequest;
import com.hitsa.bloggingsite.exceptions.SpringRedditException;
import com.hitsa.bloggingsite.model.NotificationEmail;
import com.hitsa.bloggingsite.model.User;
import com.hitsa.bloggingsite.model.VerificationToken;
import com.hitsa.bloggingsite.repository.UserRepository;
import com.hitsa.bloggingsite.repository.VerificationTokenRepository;
import com.hitsa.bloggingsite.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public void signup(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8080/api/auth/accountVerification/" + token));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token")));
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}

/**
 * we have declared final fields to following. We could have annotated them with @Autowired annotation but we didn't.
 * private final PasswordEncoder passwordEncoder;
 * private final UserRepository userRepository;
 * 
 * it is because spring boot suggests that autowired annotation should be injected mostly in constructors rather
 * than fields.
 * Although current documentation for spring framework (5.0.3) only defines two major types of injection, in reality there are three;

Constructor-based dependency injection
Setter-based dependency injection
Field-based dependency injection

Constructor-based dependency injection
In constructor-based dependency injection, the class constructor is annotated with @Autowired and includes a 
variable number of arguments with the objects to be injected.

The main advantage of constructor-based injection is that you can declare your injected fields final, as they 
will be initiated during class instantiation. This is convenient for required dependencies.

Setter-based dependency injection
In setter-based dependency injection, setter methods are annotated with @Autowired. Spring container will call 
these setter methods once the Bean is instantiated using a no-argument constructor or a no-argument static factory 
method in order to inject the Bean’s dependencies.

Field-based dependency injection
In field-based dependency injection, fields/properties are annotated with @Autowired. Spring container will 
set these fields once the class is instantiated.

Field-based dependency injection drawbacks
Disallows immutable field declaration
Field-based dependency injection won’t work on fields that are declared final/immutable as this fields must be 
instantiated at class instantiation. The only way to declare immutable dependencies is by using constructor-based 
dependency injection.

Hidden dependencies
When using a dependency injection pattern, affected classes should clearly expose these dependencies using a public 
interface either by exposing the the required dependencies in the constructor or the optional ones using methods 
(setters). When using field-based dependency injection, the class is inherently hiding these dependencies to the 
outside world.
 */