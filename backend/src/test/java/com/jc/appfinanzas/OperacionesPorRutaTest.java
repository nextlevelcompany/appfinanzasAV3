package com.jc.appfinanzas;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/**
 * Suite automatizada que ejecuta exactamente las 10 operaciones REST
 * (2 casos por operación = 20 casos) documentadas en la matriz Excel:
 * Matriz_Casos_Prueba_API_AppFinanzas_20_Casos.xlsx
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperacionesPorRutaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

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

    private int crearTransaccion(String descripcion, double monto, String tipo, String categoria, String fecha)
            throws Exception {
        String json = "{\"descripcion\":\"" + descripcion + "\",\"monto\":" + monto + ",\"tipo\":\""
                + tipo + "\",\"categoria\":\"" + categoria + "\",\"fecha\":\"" + fecha + "\"}";
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated());
        return jdbc.queryForObject("select max(id) from transacciones", Integer.class);
    }

    // =========================================================================
    // 1. POST /auth/register (AuthController)
    // =========================================================================
    @Test
    @DisplayName("Op 1: POST /auth/register - Caso 1: registro exitoso | Caso 2: duplicado 409")
    void registrarUsuarioDosCasos() throws Exception {
        // Caso 1: registro válido
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ana.prueba\",\"password\":\"ClaveSegura123\"}"))
                .andExpect(status().isCreated());

        // Caso 2: usuario duplicado
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ana.prueba\",\"password\":\"ClaveSegura123\"}"))
                .andExpect(status().isConflict());
    }

    // =========================================================================
    // 2. POST /auth/login (AuthController)
    // =========================================================================
    @Test
    @DisplayName("Op 2: POST /auth/login - Caso 1: emite JWT válido | Caso 2: clave errónea 401")
    void iniciarSesionDosCasos() throws Exception {
        // Caso 1: credenciales correctas
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("ruta-user"));

        // Caso 2: contraseña incorrecta
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ruta-user\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 3. POST /api/cuentas (RestCuentaController)
    // =========================================================================
    @Test
    @DisplayName("Op 3: POST /api/cuentas - Caso 1: crea cuenta 201 | Caso 2: saldo negativo 400")
    void registrarCuentaDosCasos() throws Exception {
        // Caso 1: cuenta válida
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ahorros\",\"tipo\":\"AHORROS\",\"saldo\":1250.50,\"moneda\":\"PEN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Ahorros"))
                .andExpect(jsonPath("$.saldo").value(1250.50));

        // Caso 2: saldo negativo
        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Inválida\",\"tipo\":\"AHORROS\",\"saldo\":-1,\"moneda\":\"PEN\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 4. GET /api/cuentas/{id} (RestCuentaController)
    // =========================================================================
    @Test
    @DisplayName("Op 4: GET /api/cuentas/{id} - Caso 1: cuenta existente 200 | Caso 2: inexistente 404")
    void consultarCuentaDosCasos() throws Exception {
        int id = crearCuenta();

        // Caso 1: ID existente devuelve atributos
        mockMvc.perform(get("/api/cuentas/{id}", id).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cuenta prueba"))
                .andExpect(jsonPath("$.tipo").value("AHORROS"))
                .andExpect(jsonPath("$.saldo").value(1200.0))
                .andExpect(jsonPath("$.moneda").value("PEN"));

        // Caso 2: ID inexistente
        mockMvc.perform(get("/api/cuentas/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 5. PUT /api/cuentas/{id} (RestCuentaController)
    // =========================================================================
    @Test
    @DisplayName("Op 5: PUT /api/cuentas/{id} - Caso 1: actualiza datos 200 | Caso 2: inexistente 404")
    void actualizarCuentaDosCasos() throws Exception {
        int id = crearCuenta();
        String cambio = "{\"nombre\":\"Cuenta sueldo\",\"tipo\":\"CORRIENTE\",\"saldo\":1800,\"moneda\":\"PEN\"}";

        // Caso 1: actualización correcta
        mockMvc.perform(put("/api/cuentas/{id}", id).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cuenta sueldo"))
                .andExpect(jsonPath("$.saldo").value(1800.0));

        // Caso 2: ID inexistente
        mockMvc.perform(put("/api/cuentas/{id}", 99999).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 6. DELETE /api/cuentas/{id} (RestCuentaController)
    // =========================================================================
    @Test
    @DisplayName("Op 6: DELETE /api/cuentas/{id} - Caso 1: elimina 204 | Caso 2: inexistente 404")
    void eliminarCuentaDosCasos() throws Exception {
        int id = crearCuenta();

        // Caso 1: eliminación y posterior 404
        mockMvc.perform(delete("/api/cuentas/{id}", id).header("Authorization", bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/cuentas/{id}", id).header("Authorization", bearer))
                .andExpect(status().isNotFound());

        // Caso 2: ID inexistente
        mockMvc.perform(delete("/api/cuentas/{id}", 99999).header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 7. POST /api/transacciones (RestTransaccionController)
    // =========================================================================
    @Test
    @DisplayName("Op 7: POST /api/transacciones - Caso 1: registra 201 | Caso 2: monto 0 o tipo inválido 400")
    void registrarTransaccionDosCasos() throws Exception {
        // Caso 1: transacción válida
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Sueldo\",\"monto\":2500,\"tipo\":\"INGRESO\",\"categoria\":\"Salario\",\"fecha\":\"2026-10-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Sueldo"))
                .andExpect(jsonPath("$.monto").value(2500.0))
                .andExpect(jsonPath("$.tipo").value("INGRESO"));

        // Caso 2: monto cero o tipo inválido
        mockMvc.perform(post("/api/transacciones").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Error\",\"monto\":0,\"tipo\":\"OTRO\",\"categoria\":\"Salario\",\"fecha\":\"2026-10-01\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 8. GET /api/transacciones (RestTransaccionController)
    // =========================================================================
    @Test
    @DisplayName("Op 8: GET /api/transacciones - Caso 1: lista vacía [] | Caso 2: lista 2 movimientos")
    void listarTransaccionesDosCasos() throws Exception {
        // Caso 1: sin transacciones
        mockMvc.perform(get("/api/transacciones").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Caso 2: tras crear un ingreso y un gasto
        crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        crearTransaccion("Mercado", 450, "GASTO", "Alimentacion", "2026-10-05");
        mockMvc.perform(get("/api/transacciones").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // =========================================================================
    // 9. GET /api/transacciones/tipo/{tipo} (RestTransaccionController)
    // =========================================================================
    @Test
    @DisplayName("Op 9: GET /api/transacciones/tipo/{tipo} - Caso 1: filtra INGRESO 200 | Caso 2: tipo desconocido 400")
    void filtrarTipoDosCasos() throws Exception {
        crearTransaccion("Sueldo", 2500, "INGRESO", "Salario", "2026-10-01");
        crearTransaccion("Mercado", 450, "GASTO", "Alimentacion", "2026-10-05");

        // Caso 1: solo ingresos
        mockMvc.perform(get("/api/transacciones/tipo/INGRESO").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("INGRESO"));

        // Caso 2: tipo desconocido
        mockMvc.perform(get("/api/transacciones/tipo/OTRO").header("Authorization", bearer))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 10. GET /api/transacciones/rango (RestTransaccionController)
    // =========================================================================
    @Test
    @DisplayName("Op 10: GET /api/transacciones/rango - Caso 1: filtra rango 200 | Caso 2: formato inválido 400")
    void filtrarRangoDosCasos() throws Exception {
        crearTransaccion("Sueldo octubre", 2500, "INGRESO", "Salario", "2026-10-15");
        crearTransaccion("Gasto noviembre", 100, "GASTO", "Otros", "2026-11-05");

        // Caso 1: dentro del rango de octubre
        mockMvc.perform(get("/api/transacciones/rango")
                .param("desde", "2026-10-01")
                .param("hasta", "2026-10-31")
                .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descripcion").value("Sueldo octubre"));

        // Caso 2: formato inválido de fecha
        mockMvc.perform(get("/api/transacciones/rango")
                .param("desde", "01-10-2026")
                .param("hasta", "2026-10-31")
                .header("Authorization", bearer))
                .andExpect(status().isBadRequest());
    }
}
