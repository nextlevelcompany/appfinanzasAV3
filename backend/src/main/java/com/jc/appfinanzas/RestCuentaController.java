package com.jc.appfinanzas;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/cuentas")
public class RestCuentaController {
    private final CuentaRepository cuentas;
    private final CategoriaRepository categorias;
    private final TransaccionRepository transacciones;

    public RestCuentaController(CuentaRepository cuentas, CategoriaRepository categorias,
            TransaccionRepository transacciones) {
        this.cuentas = cuentas;
        this.categorias = categorias;
        this.transacciones = transacciones;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Cuenta cuenta) {
        if (cuenta.getNombre() == null || cuenta.getNombre().isBlank() || cuenta.getSaldo() < 0
                || cuenta.getTipo() == null || cuenta.getMoneda() == null) {
            return ResponseEntity.badRequest().body("Nombre, tipo, moneda y saldo no negativo son obligatorios");
        }
        cuenta.setId(0);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentas.save(cuenta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> consultarPorId(@PathVariable int id) {
        return cuentas.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Cuenta> listarTodas() {
        return cuentas.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Cuenta cambios) {
        return cuentas.findById(id).map(actual -> {
            if (cambios.getNombre() == null || cambios.getNombre().isBlank() || cambios.getSaldo() < 0
                    || cambios.getTipo() == null || cambios.getMoneda() == null) {
                return ResponseEntity.badRequest().body("Datos de cuenta inválidos");
            }
            actual.setNombre(cambios.getNombre());
            actual.setTipo(cambios.getTipo());
            actual.setSaldo(cambios.getSaldo());
            actual.setMoneda(cambios.getMoneda());
            return ResponseEntity.ok(cuentas.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (!cuentas.existsById(id)) return ResponseEntity.notFound().build();
        cuentas.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> registrarCategoria(@RequestBody Categoria nueva) {
        if (nueva.getNombre() == null || nueva.getNombre().isBlank() || nueva.getTipo() == null
                || (!nueva.getTipo().equalsIgnoreCase("INGRESO") && !nueva.getTipo().equalsIgnoreCase("GASTO"))) {
            return ResponseEntity.badRequest().body("Nombre y tipo INGRESO o GASTO son obligatorios");
        }
        nueva.setId(0);
        nueva.setTipo(nueva.getTipo().toUpperCase());
        return ResponseEntity.status(HttpStatus.CREATED).body(categorias.save(nueva));
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return categorias.findAll();
    }

    @GetMapping("/resumen")
    public ResumenFinanciero generarResumen() {
        Object[] totals = transacciones.calcularTotales().get(0);
        double ingresos = ((Number) totals[0]).doubleValue();
        double gastos = ((Number) totals[1]).doubleValue();
        double balance = ingresos - gastos;
        String estado = balance > 0 ? "SUPERAVIT" : balance < 0 ? "DEFICIT" : "EQUILIBRADO";
        return new ResumenFinanciero(java.time.YearMonth.now().toString(), ingresos, gastos, balance, estado);
    }
}
