package pe.inteligenciapa.adapter.in.rest.dto;

import java.time.LocalDate;

public record EmergenciaResponse(
        String idEmergencia,
        String codigoReporte,
        LocalDate fechaOcurrencia,
        String claseFenomeno,
        String fenomeno,
        String evento,
        UbicacionResponse ubicacion,
        String afectacion,
        String redAfectada,
        String tramoAfectado,
        String sectorAfectado,
        String hechos,
        String novedades,
        String fuente,
        String estadoInicial,
        LocalDate fechaCierre,
        String estadoActual,
        LocalDate fechaCorte) {
}
