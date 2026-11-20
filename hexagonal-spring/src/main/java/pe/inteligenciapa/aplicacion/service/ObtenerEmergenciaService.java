package pe.inteligenciapa.aplicacion.service;

import pe.inteligenciapa.aplicacion.port.in.ObtenerEmergenciaUseCase;
import pe.inteligenciapa.aplicacion.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

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
