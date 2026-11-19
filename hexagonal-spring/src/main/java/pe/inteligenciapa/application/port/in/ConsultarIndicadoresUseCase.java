package pe.inteligenciapa.application.port.in;

import java.time.LocalDate;
import pe.inteligenciapa.application.model.ResumenIndicadores;

public interface ConsultarIndicadoresUseCase {
    ResumenIndicadores consultarResumen(LocalDate desde, LocalDate hasta);
}
