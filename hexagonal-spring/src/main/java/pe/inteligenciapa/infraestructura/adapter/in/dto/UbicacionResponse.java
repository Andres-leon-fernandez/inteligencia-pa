package pe.inteligenciapa.infraestructura.adapter.in.dto;

public record UbicacionResponse(
        String departamento,
        String provincia,
        String distrito,
        Double latitud,
        Double longitud) {
}
