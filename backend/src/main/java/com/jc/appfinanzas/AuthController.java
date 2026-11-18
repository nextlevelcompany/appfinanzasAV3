package com.jc.appfinanzas;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarios, RolRepository roles,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody AuthRequest request) {
        if (request.username() == null || request.username().isBlank() || request.username().length() > 80
                || request.password() == null || request.password().length() < 8) {
            return ResponseEntity.badRequest().body("Usuario obligatorio y contraseña de al menos 8 caracteres");
        }
        if (usuarios.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya está registrado");
        }
        Usuario usuario = new Usuario(request.username().trim(), passwordEncoder.encode(request.password()));
        usuario.agregarRol(roles.findByNombre("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol ROLE_USER")));
        usuarios.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(request.username()), "Bearer",
                    request.username()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }
}
