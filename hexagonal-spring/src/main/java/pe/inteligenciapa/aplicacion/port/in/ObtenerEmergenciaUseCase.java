package pe.inteligenciapa.aplicacion.port.in;

import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

public interface ObtenerEmergenciaUseCase {
    EmergenciaVial obtenerPorId(String idEmergencia);
}
