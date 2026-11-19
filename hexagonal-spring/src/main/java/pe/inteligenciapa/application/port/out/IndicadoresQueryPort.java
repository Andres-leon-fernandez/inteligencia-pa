package pe.inteligenciapa.application.port.out;

import java.time.LocalDate;
import pe.inteligenciapa.application.model.ResumenIndicadores;

public interface IndicadoresQueryPort {
    ResumenIndicadores consultarResumen(LocalDate desde, LocalDate hasta);
}
