package pe.inteligenciapa.aplicacion.modelo;

import java.time.LocalDate;

/** Rango y categorías que limitan el resumen analítico solicitado al puerto de datos. */
public record FiltroIndicadores(
        LocalDate desde,
        LocalDate hasta,
        String departamento,
        String estado,
        String evento) {
}
