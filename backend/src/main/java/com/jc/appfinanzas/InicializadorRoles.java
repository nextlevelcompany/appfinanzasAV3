package com.jc.appfinanzas;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class InicializadorRoles {
    @Bean
    ApplicationRunner inicializarRoles(RolRepository roles, UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder, CuentaRepository cuentas,
            CategoriaRepository categorias, TransaccionRepository transacciones) {
        return args -> inicializar(roles, usuarios, passwordEncoder, cuentas, categorias, transacciones);
    }

    @Transactional
    void inicializar(RolRepository roles, UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder, CuentaRepository cuentas,
            CategoriaRepository categorias, TransaccionRepository transacciones) {
        Rol user = roles.findByNombre("ROLE_USER").orElseGet(() -> roles.save(new Rol("ROLE_USER")));
        roles.findByNombre("ROLE_ADMIN").orElseGet(() -> roles.save(new Rol("ROLE_ADMIN")));

        usuarios.findAll().stream()
                .filter(usuario -> usuario.getRoles().isEmpty())
                .forEach(usuario -> {
                    usuario.agregarRol(user);
                    usuarios.save(usuario);
                });

        // Crear usuario demo por defecto si no existe
        if (!usuarios.existsByUsername("ruta-user")) {
            Usuario demo = new Usuario("ruta-user", passwordEncoder.encode("secreto123"));
            demo.agregarRol(user);
            usuarios.save(demo);
        }

        // Sembrar cuentas de prueba si está vacío
        if (cuentas.count() == 0) {
            cuentas.save(new Cuenta(0, "BCP Ahorros", "AHORROS", 4500.00, "PEN"));
            cuentas.save(new Cuenta(0, "BBVA Sueldo", "CORRIENTE", 1250.50, "PEN"));
            cuentas.save(new Cuenta(0, "Efectivo Billetera", "EFECTIVO", 320.00, "PEN"));
        }

        // Sembrar categorías de prueba si está vacío
        if (categorias.count() == 0) {
            categorias.save(new Categoria(0, "Salario", "INGRESO", 0.0, "#16803C"));
            categorias.save(new Categoria(0, "Alimentacion", "GASTO", 800.0, "#D97706"));
            categorias.save(new Categoria(0, "Servicios", "GASTO", 300.0, "#2563EB"));
            categorias.save(new Categoria(0, "Freelance", "INGRESO", 0.0, "#059669"));
        }

        // Sembrar transacciones de prueba si está vacío
        if (transacciones.count() == 0) {
            transacciones.save(new Transaccion(0, "Pago de nómina quincenal", 2800.00, "INGRESO", "Salario", java.time.LocalDate.now().minusDays(10).toString()));
            transacciones.save(new Transaccion(0, "Compra de víveres supermercado", 380.50, "GASTO", "Alimentacion", java.time.LocalDate.now().minusDays(5).toString()));
            transacciones.save(new Transaccion(0, "Pago de luz e internet", 175.00, "GASTO", "Servicios", java.time.LocalDate.now().minusDays(2).toString()));
            transacciones.save(new Transaccion(0, "Trabajo freelance diseño", 650.00, "INGRESO", "Freelance", java.time.LocalDate.now().minusDays(1).toString()));
        }
    }
}
