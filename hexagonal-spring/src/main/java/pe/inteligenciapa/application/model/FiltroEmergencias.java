package pe.inteligenciapa.application.model;

import java.time.LocalDate;

public record FiltroEmergencias(
        String departamento,
        String provincia,
        String distrito,
        String estado,
        String evento,
        String red,
        String busqueda,
        LocalDate desde,
        LocalDate hasta,
        int pagina,
        int tamano) {

    public FiltroEmergencias {
        if (pagina < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new IllegalArgumentException("El tamaño debe estar entre 1 y 100");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
    }
}
