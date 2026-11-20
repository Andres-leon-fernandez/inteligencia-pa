package pe.inteligenciapa.aplicacion.modelo;

import java.time.LocalDate;

/** Criterios de búsqueda y paginación que recibe el caso de uso de consulta. */
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
        int tamano,
        String ordenarPor,
        String direccion) {

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
        if (ordenarPor == null || !java.util.Set.of("fecha", "estado", "departamento", "evento").contains(ordenarPor)) {
            throw new IllegalArgumentException("El campo de orden debe ser fecha, estado, departamento o evento");
        }
        if (direccion == null || !java.util.Set.of("asc", "desc").contains(direccion.toLowerCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException("La dirección debe ser asc o desc");
        }
        direccion = direccion.toLowerCase(java.util.Locale.ROOT);
    }
}
