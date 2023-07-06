package it.cgmconsulting.msauth.service;

import it.cgmconsulting.msauth.entity.Authority;
import it.cgmconsulting.msauth.entity.User;
import it.cgmconsulting.msauth.payload.request.ChangeRoleRequest;
import it.cgmconsulting.msauth.payload.request.SignInRequest;
import it.cgmconsulting.msauth.payload.request.SignUpRequest;
import it.cgmconsulting.msauth.payload.response.JwtAuthenticationResponse;
import it.cgmconsulting.msauth.repository.AuthorityRepository;
import it.cgmconsulting.msauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtService;

    public ResponseEntity<?> signup(SignUpRequest request) {
        if (existsByUsernameOrEmail(request.getUsername(), request.getEmail()))
            return new ResponseEntity<>("Username or email already in use", HttpStatus.BAD_REQUEST);
        User u = fromRequestToEntity(request);
        Optional<Authority> a = authorityRepository.findByAuthorityName("ROLE_READER"); //commentare se scommento sopra
        if (!a.isPresent()) return new ResponseEntity<>("Something went wrong during registration",
                HttpStatus.UNPROCESSABLE_ENTITY);
        u.getAuthorities().add(a.get());
        u.setEnabled(true); //da commentare quando si scommenta quello sotto
        save(u);
        return new ResponseEntity<>("Signup successfully completed", HttpStatus.OK);
    }


    public ResponseEntity<?> signin(SignInRequest request) {
        /* Verificare esistenza user e correttezza password */
        Optional<User> u = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail());
               if(!u.isPresent()) return new ResponseEntity<>("Wrong username or password", HttpStatus.FORBIDDEN);
        if (!passwordEncoder.matches(request.getPassword(), u.get().getPassword())) {
            return new ResponseEntity<>("Wrong username or password", HttpStatus.FORBIDDEN);
        }
        /* Generare response con JWT                        */
//        String jwt = JwtTokenProvider.generateToken(authentication);
//        JwtAuthenticationResponse currentUser = UserPrincipal.
//                createJwtAuthenticationResponseFromUserPrincipal((UserPrincipal) authentication.getPrincipal(), jwt);
        return new ResponseEntity<>(JwtAuthenticationResponse.createJwtAuthenticationResponse(u.get(),
                                    jwtService.generateToken(u.get())), HttpStatus.OK);
    }



    protected boolean existsByUsernameOrEmail(String username, String email) {
        return userRepository.existsByUsernameOrEmail(username, email);
    }

    protected User fromRequestToEntity(SignUpRequest request) {
        return new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()));
    }

    protected void save(User user){
        userRepository.save(user);
    }


    public ResponseEntity<?> changeRoles(ChangeRoleRequest request, String adminUseId) {

        if (request.getId() == Long.parseLong(adminUseId)) {
            return new ResponseEntity<>("Admin cannot change his own roles", HttpStatus.FORBIDDEN);
        }

        Set<Authority> authorities = authorityRepository.findByAuthorityNameIn(request.getNewAuthorities());
        if (authorities.isEmpty()) {
            return new ResponseEntity<>("No valid authority selected", HttpStatus.BAD_REQUEST);
        }

        Optional<User> u = userRepository.findById(request.getId());
        if (u.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        u.get().setAuthorities(authorities);
        userRepository.save(u.get());
        return new ResponseEntity<>("Roles updated succesfuly", HttpStatus.OK);
    }
}
