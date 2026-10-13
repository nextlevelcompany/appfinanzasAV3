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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperacionesPorRutaTest {
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
                .content("{\"username\":\"ruta-user\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated());
        var login = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(login.getResponse().getContentAsString()).get("token").asText();
        bearer = "Bearer " + token;
    }

    private int crearCuenta() throws Exception {
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Cuenta prueba\",\"tipo\":\"AHORROS\",\"saldo\":1200,\"moneda\":\"PEN\"}"))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("select max(id) from cuentas", Integer.class);
    }

    private int crearCategoria() throws Exception {
        mockMvc.perform(post("/api/cuentas/categorias").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Alimentacion\",\"tipo\":\"GASTO\",\"presupuestoLimite\":500,\"colorHex\":\"#336699\"}"))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("select max(id) from categorias", Integer.class);
    }

    private int crearTransaccion(String descripcion, double monto, String tipo, String categoria, String fecha)
            throws Exception {
        String json = "{\"descripcion\":\"" + descripcion + "\",\"monto\":" + monto + ",\"tipo\":\""
                + tipo + "\",\"categoria\":\"" + categoria + "\",\"fecha\":\"" + fecha + "\"}";
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated());
        return jdbc.queryForObject("select max(id) from transacciones", Integer.class);
    }

    @Test
    @DisplayName("POST /auth/register: registra un usuario y rechaza duplicados")
    void registrarUsuarioDosCasos() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nuevo-user\",\"password\":\"clave1234\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"secreto123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/login: emite JWT con credenciales correctas y rechaza contraseña incorrecta")
    void iniciarSesionDosCasos() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/cuentas: crea una cuenta y rechaza un saldo negativo")
    void registrarCuentaDosCasos() throws Exception {
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ahorros\",\"tipo\":\"AHORROS\",\"saldo\":100,\"moneda\":\"PEN\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Inválida\",\"tipo\":\"AHORROS\",\"saldo\":-1,\"moneda\":\"PEN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cuentas/{id}: devuelve la cuenta existente o 404")
    void consultarCuentaDosCasos() throws Exception {
        int id = crearCuenta();
        mockMvc.perform(get("/api/cuentas/{id}", id).header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Cuenta prueba"));
        mockMvc.perform(get("/api/cuentas/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cuentas: devuelve una lista vacía o las cuentas guardadas")
    void listarCuentasDosCasos() throws Exception {
        mockMvc.perform(get("/api/cuentas").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        crearCuenta();
        mockMvc.perform(get("/api/cuentas").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("PUT /api/cuentas/{id}: actualiza una cuenta y responde 404 para id inexistente")
    void actualizarCuentaDosCasos() throws Exception {
        int id = crearCuenta();
        String cambio = "{\"nombre\":\"Cuenta actualizada\",\"tipo\":\"CORRIENTE\",\"saldo\":1400,\"moneda\":\"PEN\"}";
        mockMvc.perform(put("/api/cuentas/{id}", id).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Cuenta actualizada"));
        mockMvc.perform(put("/api/cuentas/{id}", 99999).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/cuentas/{id}: elimina una cuenta o responde 404")
    void eliminarCuentaDosCasos() throws Exception {
        int id = crearCuenta();
        mockMvc.perform(delete("/api/cuentas/{id}", id).header("Authorization", bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/cuentas/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/cuentas/categorias: crea una categoría y rechaza un tipo inválido")
    void registrarCategoriaDosCasos() throws Exception {
        mockMvc.perform(post("/api/cuentas/categorias").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Salario\",\"tipo\":\"INGRESO\",\"presupuestoLimite\":0,\"colorHex\":\"#00AA00\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value("INGRESO"));
        mockMvc.perform(post("/api/cuentas/categorias").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Rara\",\"tipo\":\"OTRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cuentas/categorias: lista cero o una categoría persistida")
    void listarCategoriasDosCasos() throws Exception {
        mockMvc.perform(get("/api/cuentas/categorias").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        crearCategoria();
        mockMvc.perform(get("/api/cuentas/categorias").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/cuentas/resumen: calcula totales vacíos y con movimientos")
    void resumenDosCasos() throws Exception {
        mockMvc.perform(get("/api/cuentas/resumen").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalIngresos").value(0.0))
                .andExpect(jsonPath("$.totalGastos").value(0.0));
        crearTransaccion("Sueldo", 2000, "INGRESO", "Salario", "2026-10-01");
        crearTransaccion("Mercado", 300, "GASTO", "Comida", "2026-10-02");
        mockMvc.perform(get("/api/cuentas/resumen").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalIngresos").value(2000.0))
                .andExpect(jsonPath("$.totalGastos").value(300.0)).andExpect(jsonPath("$.balanceNeto").value(1700.0));
    }

    @Test
    @DisplayName("POST /api/transacciones: guarda un movimiento y rechaza datos inválidos")
    void registrarTransaccionDosCasos() throws Exception {
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Sueldo\",\"monto\":2500,\"tipo\":\"INGRESO\",\"categoria\":\"Salario\",\"fecha\":\"2026-10-01\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.monto").value(2500.0));
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Error\",\"monto\":0,\"tipo\":\"OTRO\",\"fecha\":\"2026-10-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/transacciones/{id}: consulta un movimiento o responde 404")
    void consultarTransaccionDosCasos() throws Exception {
        int id = crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        mockMvc.perform(get("/api/transacciones/{id}", id).header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descripcion").value("Sueldo"));
        mockMvc.perform(get("/api/transacciones/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/transacciones: devuelve lista vacía o los movimientos persistidos")
    void listarTransaccionesDosCasos() throws Exception {
        mockMvc.perform(get("/api/transacciones").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        mockMvc.perform(get("/api/transacciones").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/transacciones/tipo/{tipo}: filtra INGRESO y rechaza tipo desconocido")
    void filtrarTipoDosCasos() throws Exception {
        crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        mockMvc.perform(get("/api/transacciones/tipo/INGRESO").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/transacciones/tipo/OTRO").header("Authorization", bearer))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/transacciones/categoria/{nombre}: encuentra coincidencias y permite resultado vacío")
    void filtrarCategoriaDosCasos() throws Exception {
        crearTransaccion("Mercado", 300, "GASTO", "Alimentacion", "2026-10-02");
        mockMvc.perform(get("/api/transacciones/categoria/alim").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("Alimentacion"));
        mockMvc.perform(get("/api/transacciones/categoria/transporte").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/transacciones/rango: filtra fechas y rechaza formato inválido")
    void filtrarRangoDosCasos() throws Exception {
        crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        mockMvc.perform(get("/api/transacciones/rango").param("desde", "2026-10-01")
                .param("hasta", "2026-10-31").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/transacciones/rango").param("desde", "01-10-2026")
                .param("hasta", "2026-10-31").header("Authorization", bearer))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/transacciones/{id}: actualiza un movimiento o responde 404")
    void actualizarTransaccionDosCasos() throws Exception {
        int id = crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        String cambio = "{\"descripcion\":\"Sueldo actualizado\",\"monto\":2700,\"tipo\":\"INGRESO\",\"categoria\":\"Salario\",\"fecha\":\"2026-10-01\"}";
        mockMvc.perform(put("/api/transacciones/{id}", id).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isOk()).andExpect(jsonPath("$.monto").value(2700.0));
        mockMvc.perform(put("/api/transacciones/{id}", 99999).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/transacciones/{id}: elimina un movimiento o responde 404")
    void eliminarTransaccionDosCasos() throws Exception {
        int id = crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        mockMvc.perform(delete("/api/transacciones/{id}", id).header("Authorization", bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/transacciones/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }
}
