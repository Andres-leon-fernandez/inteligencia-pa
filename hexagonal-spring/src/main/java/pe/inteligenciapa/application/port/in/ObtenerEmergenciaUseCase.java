package pe.inteligenciapa.application.port.in;

import pe.inteligenciapa.domain.model.EmergenciaVial;

public interface ObtenerEmergenciaUseCase {
    EmergenciaVial obtenerPorId(String idEmergencia);
}
