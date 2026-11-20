package pe.inteligenciapa.application.service;

import pe.inteligenciapa.application.model.FiltroIndicadores;
import pe.inteligenciapa.application.model.ResumenIndicadores;
import pe.inteligenciapa.application.port.in.ConsultarIndicadoresUseCase;
import pe.inteligenciapa.application.port.out.IndicadoresQueryPort;

public class ConsultarIndicadoresService implements ConsultarIndicadoresUseCase {
    private final IndicadoresQueryPort indicadoresQueryPort;

    public ConsultarIndicadoresService(IndicadoresQueryPort indicadoresQueryPort) {
        this.indicadoresQueryPort = indicadoresQueryPort;
    }

    @Override
    public ResumenIndicadores consultarResumen(FiltroIndicadores filtro) {
        if (filtro.desde() != null && filtro.hasta() != null && filtro.desde().isAfter(filtro.hasta())) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return indicadoresQueryPort.consultarResumen(filtro);
    }
}
