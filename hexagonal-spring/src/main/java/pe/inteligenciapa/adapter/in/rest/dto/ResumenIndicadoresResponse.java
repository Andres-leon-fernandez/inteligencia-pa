package pe.inteligenciapa.adapter.in.rest.dto;

import java.time.LocalDate;
import java.util.List;

public record ResumenIndicadoresResponse(
        LocalDate desde,
        LocalDate hasta,
        long totalReportes,
        List<IndicadorItemResponse> porEstado,
        List<IndicadorItemResponse> porDepartamento,
        List<IndicadorItemResponse> porEvento) {
}
