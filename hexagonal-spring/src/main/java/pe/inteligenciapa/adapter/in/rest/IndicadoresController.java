package pe.inteligenciapa.adapter.in.rest;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.inteligenciapa.adapter.in.rest.dto.IndicadorItemResponse;
import pe.inteligenciapa.adapter.in.rest.dto.ResumenIndicadoresResponse;
import pe.inteligenciapa.application.model.ResumenIndicadores;
import pe.inteligenciapa.application.port.in.ConsultarIndicadoresUseCase;

@RestController
@RequestMapping("/api/v1/indicadores")
public class IndicadoresController {
    private final ConsultarIndicadoresUseCase consultarIndicadores;

    public IndicadoresController(ConsultarIndicadoresUseCase consultarIndicadores) {
        this.consultarIndicadores = consultarIndicadores;
    }

    @GetMapping("/resumen")
    public ResumenIndicadoresResponse resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ResumenIndicadores resumen = consultarIndicadores.consultarResumen(desde, hasta);
        return new ResumenIndicadoresResponse(
                resumen.desde(),
                resumen.hasta(),
                resumen.fechaInicialDatos(),
                resumen.fechaMaximaDatos(),
                resumen.totalReportes(),
                resumen.porEstado().stream()
                        .map(item -> new IndicadorItemResponse(item.categoria(), item.cantidad()))
                        .toList(),
                resumen.porDepartamento().stream()
                        .map(item -> new IndicadorItemResponse(item.categoria(), item.cantidad()))
                        .toList(),
                resumen.porEvento().stream()
                        .map(item -> new IndicadorItemResponse(item.categoria(), item.cantidad()))
                        .toList());
    }
}
