package pe.inteligenciapa.infraestructura.adapter.in.mapper;

import pe.inteligenciapa.infraestructura.adapter.in.dto.EmergenciaResponse;
import pe.inteligenciapa.infraestructura.adapter.in.dto.UbicacionResponse;
import pe.inteligenciapa.dominio.modelo.Coordenadas;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;
import pe.inteligenciapa.dominio.modelo.UbicacionVial;

public final class EmergenciaRestMapper {
    private EmergenciaRestMapper() {
    }

    public static EmergenciaResponse toResponse(EmergenciaVial emergencia) {
        UbicacionVial ubicacion = emergencia.ubicacion();
        Coordenadas coordenadas = ubicacion == null ? null : ubicacion.coordenadas();
        UbicacionResponse ubicacionResponse = ubicacion == null
                ? null
                : new UbicacionResponse(
                        ubicacion.departamento(),
                        ubicacion.provincia(),
                        ubicacion.distrito(),
                        coordenadas == null ? null : coordenadas.latitud(),
                        coordenadas == null ? null : coordenadas.longitud());

        return new EmergenciaResponse(
                emergencia.idEmergencia(),
                emergencia.codigoReporte(),
                emergencia.fechaOcurrencia(),
                emergencia.claseFenomeno(),
                emergencia.fenomeno(),
                emergencia.evento(),
                ubicacionResponse,
                emergencia.afectacion(),
                emergencia.redAfectada(),
                emergencia.tramoAfectado(),
                emergencia.sectorAfectado(),
                emergencia.hechos(),
                emergencia.novedades(),
                emergencia.fuente(),
                emergencia.estadoInicial(),
                emergencia.fechaCierre(),
                emergencia.estadoActual(),
                emergencia.fechaCorte());
    }
}
