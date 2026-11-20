package pe.inteligenciapa.infraestructura.adapter.in.dto;

import java.time.LocalDate;
import java.util.List;

public record ResumenIndicadoresResponse(
        LocalDate desde,
        LocalDate hasta,
        LocalDate fechaInicialDatos,
        LocalDate fechaMaximaDatos,
        long totalReportes,
        List<IndicadorItemResponse> porEstado,
        List<IndicadorItemResponse> porDepartamento,
        List<IndicadorItemResponse> porEvento) {
}
