package pe.inteligenciapa.adapter.in.rest.dto;

public record UbicacionResponse(
        String departamento,
        String provincia,
        String distrito,
        Double latitud,
        Double longitud) {
}
