package pe.inteligenciapa.adapter.in.rest;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.inteligenciapa.adapter.in.rest.dto.EmergenciaResponse;
import pe.inteligenciapa.adapter.in.rest.dto.PaginaResponse;
import pe.inteligenciapa.adapter.in.rest.mapper.EmergenciaRestMapper;
import pe.inteligenciapa.application.model.FiltroEmergencias;
import pe.inteligenciapa.application.model.PaginaResultado;
import pe.inteligenciapa.application.port.in.BuscarEmergenciasUseCase;
import pe.inteligenciapa.application.port.in.ObtenerEmergenciaUseCase;
import pe.inteligenciapa.domain.model.EmergenciaVial;

@RestController
@RequestMapping("/api/v1/emergencias")
public class EmergenciaController {
    private final BuscarEmergenciasUseCase buscarEmergencias;
    private final ObtenerEmergenciaUseCase obtenerEmergencia;

    public EmergenciaController(
            BuscarEmergenciasUseCase buscarEmergencias,
            ObtenerEmergenciaUseCase obtenerEmergencia) {
        this.buscarEmergencias = buscarEmergencias;
        this.obtenerEmergencia = obtenerEmergencia;
    }

    @GetMapping
    public PaginaResponse<EmergenciaResponse> buscar(
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String evento,
            @RequestParam(required = false) String red,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        FiltroEmergencias filtro = new FiltroEmergencias(
                departamento, provincia, distrito, estado, evento, red, busqueda, desde, hasta, pagina, tamano);
        PaginaResultado<EmergenciaVial> resultado = buscarEmergencias.buscar(filtro);
        return new PaginaResponse<>(
                resultado.contenido().stream().map(EmergenciaRestMapper::toResponse).toList(),
                resultado.pagina(),
                resultado.tamano(),
                resultado.totalElementos(),
                resultado.totalPaginas());
    }

    @GetMapping("/{idEmergencia}")
    public EmergenciaResponse obtener(@PathVariable String idEmergencia) {
        return EmergenciaRestMapper.toResponse(obtenerEmergencia.obtenerPorId(idEmergencia));
    }
}
