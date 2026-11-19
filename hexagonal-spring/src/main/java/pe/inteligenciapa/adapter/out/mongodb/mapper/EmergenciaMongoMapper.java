package pe.inteligenciapa.adapter.out.mongodb.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import pe.inteligenciapa.adapter.out.mongodb.document.EmergenciaMongoDocument;
import pe.inteligenciapa.domain.model.Coordenadas;
import pe.inteligenciapa.domain.model.EmergenciaVial;
import pe.inteligenciapa.domain.model.UbicacionVial;

public final class EmergenciaMongoMapper {
    private EmergenciaMongoMapper() {
    }

    public static EmergenciaVial toDomain(EmergenciaMongoDocument document) {
        Coordenadas coordenadas = document.latitud() == null && document.longitud() == null
                ? null
                : new Coordenadas(document.latitud(), document.longitud());
        UbicacionVial ubicacion = new UbicacionVial(
                document.departamento(), document.provincia(), document.distrito(), coordenadas);

        return new EmergenciaVial(
                document.idEmergencia(),
                document.codigoReporte(),
                toLocalDate(document.fechaOcurrencia()),
                document.claseFenomeno(),
                document.fenomeno(),
                document.evento(),
                ubicacion,
                document.afectacion(),
                document.redAfectada(),
                document.tramoAfectado(),
                document.sectorAfectado(),
                document.hechos(),
                document.novedades(),
                document.fuente(),
                document.estadoInicial(),
                toLocalDate(document.fechaCierre()),
                document.estadoActual(),
                toLocalDate(document.fechaCorte()));
    }

    private static LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }
}
