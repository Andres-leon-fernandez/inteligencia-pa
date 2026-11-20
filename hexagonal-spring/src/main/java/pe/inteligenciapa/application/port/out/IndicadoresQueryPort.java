package pe.inteligenciapa.application.port.out;

import pe.inteligenciapa.application.model.FiltroIndicadores;
import pe.inteligenciapa.application.model.ResumenIndicadores;

public interface IndicadoresQueryPort {
    ResumenIndicadores consultarResumen(FiltroIndicadores filtro);
}
