package pe.inteligenciapa.application.port.in;

import pe.inteligenciapa.application.model.FiltroIndicadores;
import pe.inteligenciapa.application.model.ResumenIndicadores;

public interface ConsultarIndicadoresUseCase {
    ResumenIndicadores consultarResumen(FiltroIndicadores filtro);
}
