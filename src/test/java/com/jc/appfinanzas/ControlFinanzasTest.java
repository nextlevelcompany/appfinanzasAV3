package com.jc.appfinanzas;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControlFinanzasTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    private String bearer;

    @BeforeEach
    void preparar() throws Exception {
        jdbc.update("delete from transacciones");
        jdbc.update("delete from usuario_rol");
        jdbc.update("delete from categorias");
        jdbc.update("delete from cuentas");
        jdbc.update("delete from usuarios");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"prueba\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"prueba\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk()).andReturn();
        String body = login.getResponse().getContentAsString();
        bearer = "Bearer " + com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("Transacciones caso 1: CRUD, filtros y resumen usan datos persistidos")
    void operacionesTransaccionPersistenYFiltran() throws Exception {
        String ingreso = "{\"descripcion\":\"Sueldo\",\"monto\":3500,\"tipo\":\"INGRESO\",\"categoria\":\"Salario\",\"fecha\":\"2026-09-01\"}";
        String gasto = "{\"descripcion\":\"Mercado\",\"monto\":450,\"tipo\":\"GASTO\",\"categoria\":\"Alimentacion\",\"fecha\":\"2026-09-05\"}";
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(ingreso))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.descripcion").value("Sueldo"));
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(gasto))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value("GASTO"));
        mockMvc.perform(get("/api/transacciones").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        int id = jdbc.queryForObject("select min(id) from transacciones", Integer.class);
        mockMvc.perform(get("/api/transacciones/{id}", id).header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.fecha").value("2026-09-01"));
        mockMvc.perform(get("/api/transacciones/tipo/INGRESO").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/transacciones/categoria/alim").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].categoria").value("Alimentacion"));
        mockMvc.perform(get("/api/transacciones/rango").param("desde", "2026-09-01").param("hasta", "2026-09-30").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/cuentas/resumen").header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(3500.0)).andExpect(jsonPath("$.totalGastos").value(450.0));
        String editado = "{\"descripcion\":\"Sueldo actualizado\",\"monto\":3600,\"tipo\":\"INGRESO\",\"categoria\":\"Salario\",\"fecha\":\"2026-09-01\"}";
        mockMvc.perform(put("/api/transacciones/{id}", id).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(editado))
                .andExpect(status().isOk()).andExpect(jsonPath("$.monto").value(3600.0));
        mockMvc.perform(delete("/api/transacciones/{id}", id).header("Authorization", bearer)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Transacciones caso 2: entradas inválidas y recursos inexistentes tienen respuestas definidas")
    void validaTransaccionYNoEncontrados() throws Exception {
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"mal\",\"monto\":-1,\"tipo\":\"OTRO\",\"fecha\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/transacciones/999").header("Authorization", bearer)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/transacciones/tipo/OTRO").header("Authorization", bearer)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/transacciones/rango").param("desde", "2026-12-31").param("hasta", "2026-01-01").header("Authorization", bearer))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cuentas caso 1: CRUD y categorías se guardan en la base")
    void operacionesCuentaYCategoriaPersisten() throws Exception {
        String cuenta = "{\"nombre\":\"Cuenta sueldo\",\"tipo\":\"AHORROS\",\"saldo\":2500,\"moneda\":\"PEN\"}";
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(cuenta))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.moneda").value("PEN"));
        int id = jdbc.queryForObject("select min(id) from cuentas", Integer.class);
        mockMvc.perform(get("/api/cuentas").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/cuentas/{id}", id).header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.saldo").value(2500.0));
        String actualizada = "{\"nombre\":\"Ahorros\",\"tipo\":\"AHORROS\",\"saldo\":3000,\"moneda\":\"PEN\"}";
        mockMvc.perform(put("/api/cuentas/{id}", id).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(actualizada))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ahorros"));
        mockMvc.perform(post("/api/cuentas/categorias").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Alimentacion\",\"tipo\":\"GASTO\",\"presupuestoLimite\":600,\"colorHex\":\"#FF5733\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        mockMvc.perform(get("/api/cuentas/categorias").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(delete("/api/cuentas/{id}", id).header("Authorization", bearer)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Cuentas caso 2: saldo, campos y recursos inexistentes se validan")
    void validaCuentaYNoEncontrados() throws Exception {
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Inválida\",\"tipo\":\"AHORROS\",\"saldo\":-1,\"moneda\":\"PEN\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/cuentas/999").header("Authorization", bearer)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/cuentas/categorias").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Rara\",\"tipo\":\"OTRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Autenticación caso 1: usuario BCrypt en base de datos obtiene JWT válido")
    void registraEIniciaSesion() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"prueba\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty()).andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Autenticación caso 2: acceso sin token y credenciales erróneas se rechazan")
    void rechazaAccesoSinTokenYCredencialesErroneas() throws Exception {
        mockMvc.perform(get("/api/cuentas")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"prueba\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Roles caso 1: el registro persiste ROLE_USER y no concede acceso administrativo")
    void usuarioRegistradoTieneRolUserYNoAdmin() throws Exception {
        Integer userId = jdbc.queryForObject("select id from usuarios where username = ?", Integer.class, "prueba");
        Integer userRoleId = jdbc.queryForObject("select id from rol where nombre = ?", Integer.class, "ROLE_USER");
        org.junit.jupiter.api.Assertions.assertEquals(1,
                jdbc.queryForObject("select count(*) from usuario_rol where id_usuario = ? and id_rol = ?",
                        Integer.class, userId, userRoleId));
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", bearer))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Roles caso 2: ROLE_ADMIN puede consultar usuarios sin exponer contraseñas")
    void administradorAccedeAlRecursoProtegido() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin-prueba\",\"password\":\"adminsecreto123\"}"))
                .andExpect(status().isCreated());
        Integer adminId = jdbc.queryForObject("select id from usuarios where username = ?", Integer.class, "admin-prueba");
        Integer adminRoleId = jdbc.queryForObject("select id from rol where nombre = ?", Integer.class, "ROLE_ADMIN");
        jdbc.update("insert into usuario_rol (id_usuario, id_rol) values (?, ?)", adminId, adminRoleId);
        MvcResult login = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin-prueba\",\"password\":\"adminsecreto123\"}"))
                .andExpect(status().isOk()).andReturn();
        String tokenAdmin = "Bearer " + com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(login.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(get("/api/admin/usuarios").header("Authorization", tokenAdmin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].roles").isArray())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))));
    }

    @Test
    @DisplayName("Inicio público: la página principal explica cómo usar la API")
    void paginaPrincipalEsPublica() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("La aplicación está activa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("POST /auth/login")));
    }
}
