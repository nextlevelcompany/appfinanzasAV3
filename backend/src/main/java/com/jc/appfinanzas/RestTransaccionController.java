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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/transacciones")
public class RestTransaccionController {
    private final TransaccionRepository transacciones;

    public RestTransaccionController(TransaccionRepository transacciones) {
        this.transacciones = transacciones;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Transaccion nueva) {
        if (nueva.getDescripcion() == null || nueva.getDescripcion().isBlank() || nueva.getMonto() <= 0
                || nueva.getTipo() == null || nueva.getFecha() == null
                || (!nueva.getTipo().equalsIgnoreCase("INGRESO") && !nueva.getTipo().equalsIgnoreCase("GASTO"))) {
            return ResponseEntity.badRequest().body("Descripción, monto positivo, tipo y fecha son obligatorios");
        }
        nueva.setId(0);
        nueva.setTipo(nueva.getTipo().toUpperCase());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacciones.save(nueva));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaccion> consultarPorId(@PathVariable int id) {
        return transacciones.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Transaccion> listarTodas() {
        return transacciones.findAll();
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> filtrarPorTipo(@PathVariable String tipo) {
        if (!tipo.equalsIgnoreCase("INGRESO") && !tipo.equalsIgnoreCase("GASTO")) {
            return ResponseEntity.badRequest().body("El tipo debe ser INGRESO o GASTO");
        }
        return ResponseEntity.ok(transacciones.findByTipoIgnoreCase(tipo));
    }

    @GetMapping("/categoria/{nombre}")
    public List<Transaccion> filtrarPorCategoria(@PathVariable String nombre) {
        return transacciones.findByCategoriaIgnoreCaseContaining(nombre);
    }

    @GetMapping("/rango")
    public ResponseEntity<?> filtrarPorFecha(@RequestParam String desde, @RequestParam String hasta) {
        try {
            if (java.time.LocalDate.parse(desde).isAfter(java.time.LocalDate.parse(hasta))) {
                return ResponseEntity.badRequest().body("La fecha desde debe ser anterior o igual a hasta");
            }
            return ResponseEntity.ok(transacciones.findByFechaBetween(desde, hasta));
        } catch (java.time.format.DateTimeParseException ex) {
            return ResponseEntity.badRequest().body("Las fechas deben tener formato YYYY-MM-DD");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Transaccion cambios) {
        return transacciones.findById(id).map(actual -> {
            if (cambios.getDescripcion() == null || cambios.getDescripcion().isBlank() || cambios.getMonto() <= 0
                    || cambios.getTipo() == null || cambios.getFecha() == null
                    || (!cambios.getTipo().equalsIgnoreCase("INGRESO") && !cambios.getTipo().equalsIgnoreCase("GASTO"))) {
                return ResponseEntity.badRequest().body("Datos de transacción inválidos");
            }
            actual.setDescripcion(cambios.getDescripcion());
            actual.setMonto(cambios.getMonto());
            actual.setTipo(cambios.getTipo().toUpperCase());
            actual.setCategoria(cambios.getCategoria());
            actual.setFecha(cambios.getFecha());
            return ResponseEntity.ok(transacciones.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (!transacciones.existsById(id)) return ResponseEntity.notFound().build();
        transacciones.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
