package com.jc.appfinanzas;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UsuarioRepository usuarios;

    public AdminController(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResumen> listarUsuarios() {
        return usuarios.findAll().stream()
                .map(usuario -> new UsuarioResumen(usuario.getId(), usuario.getUsername(),
                        usuario.getRoles().stream().map(Rol::getNombre).sorted().toList()))
                .toList();
    }

    public record UsuarioResumen(Integer id, String username, List<String> roles) {}
}
