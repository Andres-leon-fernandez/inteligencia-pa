package pe.inteligenciapa.dominio.modelo;

import java.time.LocalDate;

/** Reporte vial del dominio; no depende del formato de Mongo ni del JSON REST. */
public record EmergenciaVial(
        String idEmergencia,
        String codigoReporte,
        LocalDate fechaOcurrencia,
        String claseFenomeno,
        String fenomeno,
        String evento,
        UbicacionVial ubicacion,
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
