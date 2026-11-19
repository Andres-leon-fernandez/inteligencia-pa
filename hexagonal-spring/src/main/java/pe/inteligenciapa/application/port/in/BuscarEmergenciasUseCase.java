package pe.inteligenciapa.application.port.in;

import pe.inteligenciapa.application.model.FiltroEmergencias;
import pe.inteligenciapa.application.model.PaginaResultado;
import pe.inteligenciapa.domain.model.EmergenciaVial;

public interface BuscarEmergenciasUseCase {
    PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro);
}
