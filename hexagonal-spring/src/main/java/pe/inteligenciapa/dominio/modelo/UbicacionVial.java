package pe.inteligenciapa.dominio.modelo;

/** Ubicación administrativa y coordenadas asociadas a una emergencia vial. */
public record UbicacionVial(
        String departamento,
        String provincia,
        String distrito,
        Coordenadas coordenadas) {
}
