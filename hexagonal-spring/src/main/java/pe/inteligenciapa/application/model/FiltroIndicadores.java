package pe.inteligenciapa.application.model;

import java.time.LocalDate;

public record FiltroIndicadores(
        LocalDate desde,
        LocalDate hasta,
        String departamento,
        String estado,
        String evento) {
}
