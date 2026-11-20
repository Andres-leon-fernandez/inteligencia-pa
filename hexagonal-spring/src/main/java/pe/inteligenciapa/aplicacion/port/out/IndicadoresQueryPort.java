package pe.inteligenciapa.aplicacion.port.out;

import pe.inteligenciapa.aplicacion.modelo.FiltroIndicadores;
import pe.inteligenciapa.aplicacion.modelo.ResumenIndicadores;

public interface IndicadoresQueryPort {
    ResumenIndicadores consultarResumen(FiltroIndicadores filtro);
}
