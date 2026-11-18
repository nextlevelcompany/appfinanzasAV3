package com.jc.appfinanzas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
    List<Transaccion> findByTipoIgnoreCase(String tipo);

    List<Transaccion> findByCategoriaIgnoreCaseContaining(String categoria);

    List<Transaccion> findByFechaBetween(String desde, String hasta);

    @Query("select coalesce(sum(case when upper(t.tipo) = 'INGRESO' then t.monto else 0 end), 0), "
            + "coalesce(sum(case when upper(t.tipo) = 'GASTO' then t.monto else 0 end), 0) from Transaccion t")
    List<Object[]> calcularTotales();
}
