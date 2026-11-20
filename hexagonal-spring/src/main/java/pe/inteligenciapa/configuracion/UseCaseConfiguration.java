package pe.inteligenciapa.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.inteligenciapa.aplicacion.port.in.BuscarEmergenciasUseCase;
import pe.inteligenciapa.aplicacion.port.in.ConsultarIndicadoresUseCase;
import pe.inteligenciapa.aplicacion.port.in.ObtenerEmergenciaUseCase;
import pe.inteligenciapa.aplicacion.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.aplicacion.port.out.IndicadoresQueryPort;
import pe.inteligenciapa.aplicacion.service.BuscarEmergenciasService;
import pe.inteligenciapa.aplicacion.service.ConsultarIndicadoresService;
import pe.inteligenciapa.aplicacion.service.ObtenerEmergenciaService;

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
