package pe.inteligenciapa.aplicacion.service;

import pe.inteligenciapa.aplicacion.modelo.FiltroEmergencias;
import pe.inteligenciapa.aplicacion.modelo.PaginaResultado;
import pe.inteligenciapa.aplicacion.port.in.BuscarEmergenciasUseCase;
import pe.inteligenciapa.aplicacion.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

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
