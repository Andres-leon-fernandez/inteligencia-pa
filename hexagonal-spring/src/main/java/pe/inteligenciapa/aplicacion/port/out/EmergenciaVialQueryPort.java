package pe.inteligenciapa.aplicacion.port.out;

import java.util.Optional;
import pe.inteligenciapa.aplicacion.modelo.FiltroEmergencias;
import pe.inteligenciapa.aplicacion.modelo.PaginaResultado;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

public interface EmergenciaVialQueryPort {
    PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro);

    Optional<EmergenciaVial> obtenerPorId(String idEmergencia);
}
