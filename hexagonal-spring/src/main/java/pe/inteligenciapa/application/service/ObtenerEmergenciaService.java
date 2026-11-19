package pe.inteligenciapa.application.service;

import pe.inteligenciapa.application.port.in.ObtenerEmergenciaUseCase;
import pe.inteligenciapa.application.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.domain.model.EmergenciaVial;

public class ObtenerEmergenciaService implements ObtenerEmergenciaUseCase {
    private final EmergenciaVialQueryPort emergenciaQueryPort;

    public ObtenerEmergenciaService(EmergenciaVialQueryPort emergenciaQueryPort) {
        this.emergenciaQueryPort = emergenciaQueryPort;
    }

    @Override
    public EmergenciaVial obtenerPorId(String idEmergencia) {
        return emergenciaQueryPort.obtenerPorId(idEmergencia)
                .orElseThrow(() -> new EmergenciaNoEncontradaException(idEmergencia));
    }
}
