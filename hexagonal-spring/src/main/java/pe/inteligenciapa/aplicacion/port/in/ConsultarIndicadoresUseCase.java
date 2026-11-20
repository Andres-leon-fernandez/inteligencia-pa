package pe.inteligenciapa.aplicacion.port.in;

import pe.inteligenciapa.aplicacion.modelo.FiltroIndicadores;
import pe.inteligenciapa.aplicacion.modelo.ResumenIndicadores;

public interface ConsultarIndicadoresUseCase {
    ResumenIndicadores consultarResumen(FiltroIndicadores filtro);
}
