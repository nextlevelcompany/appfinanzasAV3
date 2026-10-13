package com.jc.appfinanzas;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class InicializadorRoles {
    @Bean
    ApplicationRunner inicializarRoles(RolRepository roles, UsuarioRepository usuarios) {
        return args -> inicializar(roles, usuarios);
    }

    @Transactional
    void inicializar(RolRepository roles, UsuarioRepository usuarios) {
        Rol user = roles.findByNombre("ROLE_USER").orElseGet(() -> roles.save(new Rol("ROLE_USER")));
        roles.findByNombre("ROLE_ADMIN").orElseGet(() -> roles.save(new Rol("ROLE_ADMIN")));
        usuarios.findAll().stream()
                .filter(usuario -> usuario.getRoles().isEmpty())
                .forEach(usuario -> {
                    usuario.agregarRol(user);
                    usuarios.save(usuario);
                });
    }
}
