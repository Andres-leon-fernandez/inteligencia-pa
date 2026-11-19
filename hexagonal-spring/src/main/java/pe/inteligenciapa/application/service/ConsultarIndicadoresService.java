package pe.inteligenciapa.application.service;

import java.time.LocalDate;
import pe.inteligenciapa.application.model.ResumenIndicadores;
import pe.inteligenciapa.application.port.in.ConsultarIndicadoresUseCase;
import pe.inteligenciapa.application.port.out.IndicadoresQueryPort;

public class ConsultarIndicadoresService implements ConsultarIndicadoresUseCase {
    private final IndicadoresQueryPort indicadoresQueryPort;

    public ConsultarIndicadoresService(IndicadoresQueryPort indicadoresQueryPort) {
        this.indicadoresQueryPort = indicadoresQueryPort;
    }

    @Override
    public ResumenIndicadores consultarResumen(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return indicadoresQueryPort.consultarResumen(desde, hasta);
    }
}
