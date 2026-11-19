package pe.inteligenciapa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.inteligenciapa.application.port.in.BuscarEmergenciasUseCase;
import pe.inteligenciapa.application.port.in.ConsultarIndicadoresUseCase;
import pe.inteligenciapa.application.port.in.ObtenerEmergenciaUseCase;
import pe.inteligenciapa.application.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.application.port.out.IndicadoresQueryPort;
import pe.inteligenciapa.application.service.BuscarEmergenciasService;
import pe.inteligenciapa.application.service.ConsultarIndicadoresService;
import pe.inteligenciapa.application.service.ObtenerEmergenciaService;

@Configuration
public class UseCaseConfiguration {
    @Bean
    BuscarEmergenciasUseCase buscarEmergenciasUseCase(EmergenciaVialQueryPort queryPort) {
        return new BuscarEmergenciasService(queryPort);
    }

    @Bean
    ObtenerEmergenciaUseCase obtenerEmergenciaUseCase(EmergenciaVialQueryPort queryPort) {
        return new ObtenerEmergenciaService(queryPort);
    }

    @Bean
    ConsultarIndicadoresUseCase consultarIndicadoresUseCase(IndicadoresQueryPort queryPort) {
        return new ConsultarIndicadoresService(queryPort);
    }
}
