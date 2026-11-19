package pe.inteligenciapa.domain.model;

public record UbicacionVial(
        String departamento,
        String provincia,
        String distrito,
        Coordenadas coordenadas) {
}
