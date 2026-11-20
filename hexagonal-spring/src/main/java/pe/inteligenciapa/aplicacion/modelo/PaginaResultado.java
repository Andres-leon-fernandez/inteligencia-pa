package pe.inteligenciapa.aplicacion.modelo;

import java.util.List;

public record PaginaResultado<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {
}
