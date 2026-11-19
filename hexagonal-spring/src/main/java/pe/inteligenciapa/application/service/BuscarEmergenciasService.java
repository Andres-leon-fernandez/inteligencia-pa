package pe.inteligenciapa.application.service;

import pe.inteligenciapa.application.model.FiltroEmergencias;
import pe.inteligenciapa.application.model.PaginaResultado;
import pe.inteligenciapa.application.port.in.BuscarEmergenciasUseCase;
import pe.inteligenciapa.application.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.domain.model.EmergenciaVial;

public class BuscarEmergenciasService implements BuscarEmergenciasUseCase {
    private final EmergenciaVialQueryPort emergenciaQueryPort;

    public BuscarEmergenciasService(EmergenciaVialQueryPort emergenciaQueryPort) {
        this.emergenciaQueryPort = emergenciaQueryPort;
    }

    @Override
    public PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro) {
        return emergenciaQueryPort.buscar(filtro);
    }
}
