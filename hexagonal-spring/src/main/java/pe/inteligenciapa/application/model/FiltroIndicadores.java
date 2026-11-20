package pe.inteligenciapa.application.model;

import java.time.LocalDate;

/** Rango y categorías que limitan el resumen analítico solicitado al puerto de datos. */
public record FiltroIndicadores(
        LocalDate desde,
        LocalDate hasta,
        String departamento,
        String estado,
        String evento) {
}
