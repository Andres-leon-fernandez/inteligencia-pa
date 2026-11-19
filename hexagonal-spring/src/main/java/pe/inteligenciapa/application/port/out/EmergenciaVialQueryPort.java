package pe.inteligenciapa.application.port.out;

import java.util.Optional;
import pe.inteligenciapa.application.model.FiltroEmergencias;
import pe.inteligenciapa.application.model.PaginaResultado;
import pe.inteligenciapa.domain.model.EmergenciaVial;

public interface EmergenciaVialQueryPort {
    PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro);

    Optional<EmergenciaVial> obtenerPorId(String idEmergencia);
}
