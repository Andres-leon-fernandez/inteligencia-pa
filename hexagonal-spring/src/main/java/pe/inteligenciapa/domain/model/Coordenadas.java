package pe.inteligenciapa.domain.model;

/** Par geográfico opcional; valida rangos y exige que ambas coordenadas vengan juntas. */
public record Coordenadas(Double latitud, Double longitud) {
    public Coordenadas {
        if ((latitud == null) != (longitud == null)) {
            throw new IllegalArgumentException("Latitud y longitud deben venir juntas");
        }
        if (latitud != null && (latitud < -90 || latitud > 90)) {
            throw new IllegalArgumentException("Latitud fuera de rango");
        }
        if (longitud != null && (longitud < -180 || longitud > 180)) {
            throw new IllegalArgumentException("Longitud fuera de rango");
        }
    }
}
