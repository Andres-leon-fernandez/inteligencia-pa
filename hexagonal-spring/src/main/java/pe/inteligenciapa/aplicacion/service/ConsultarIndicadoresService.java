package pe.inteligenciapa.aplicacion.service;

import pe.inteligenciapa.aplicacion.modelo.FiltroIndicadores;
import pe.inteligenciapa.aplicacion.modelo.ResumenIndicadores;
import pe.inteligenciapa.aplicacion.port.in.ConsultarIndicadoresUseCase;
import pe.inteligenciapa.aplicacion.port.out.IndicadoresQueryPort;

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
