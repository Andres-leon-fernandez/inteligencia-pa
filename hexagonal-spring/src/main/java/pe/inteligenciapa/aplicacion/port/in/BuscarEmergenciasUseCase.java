package pe.inteligenciapa.aplicacion.port.in;

import pe.inteligenciapa.aplicacion.modelo.FiltroEmergencias;
import pe.inteligenciapa.aplicacion.modelo.PaginaResultado;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

public interface BuscarEmergenciasUseCase {
    PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro);
}
