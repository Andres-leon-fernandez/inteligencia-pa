package pe.inteligenciapa.application.model;

import java.time.LocalDate;
import java.util.List;

public record ResumenIndicadores(
        LocalDate desde,
        LocalDate hasta,
        LocalDate fechaInicialDatos,
        LocalDate fechaMaximaDatos,
        long totalReportes,
        List<IndicadorItem> porEstado,
        List<IndicadorItem> porDepartamento,
        List<IndicadorItem> porEvento) {
}
