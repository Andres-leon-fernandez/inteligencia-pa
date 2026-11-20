package pe.inteligenciapa.infraestructura.adapter.out.mongodb;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pe.inteligenciapa.infraestructura.adapter.out.mongodb.document.EmergenciaMongoDocument;
import pe.inteligenciapa.infraestructura.adapter.out.mongodb.mapper.EmergenciaMongoMapper;
import pe.inteligenciapa.aplicacion.modelo.FiltroEmergencias;
import pe.inteligenciapa.aplicacion.modelo.FiltroIndicadores;
import pe.inteligenciapa.aplicacion.modelo.IndicadorItem;
import pe.inteligenciapa.aplicacion.modelo.PaginaResultado;
import pe.inteligenciapa.aplicacion.modelo.ResumenIndicadores;
import pe.inteligenciapa.aplicacion.port.out.EmergenciaVialQueryPort;
import pe.inteligenciapa.aplicacion.port.out.IndicadoresQueryPort;
import pe.inteligenciapa.dominio.modelo.EmergenciaVial;

@Repository
public class EmergenciaMongoAdapter implements EmergenciaVialQueryPort, IndicadoresQueryPort {
    private final MongoTemplate mongoTemplate;

    public EmergenciaMongoAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PaginaResultado<EmergenciaVial> buscar(FiltroEmergencias filtro) {
        Query query = crearConsulta(filtros(filtro));
        long total = mongoTemplate.count(query, EmergenciaMongoDocument.class);

        String campo = Map.of(
                "fecha", "fecha_ocurrencia",
                "estado", "estado_actual",
                "departamento", "departamento",
                "evento", "evento").get(filtro.ordenarPor());
        Sort.Direction direccion = "asc".equals(filtro.direccion()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        query.with(Sort.by(direccion, campo).and(Sort.by(Sort.Direction.ASC, "id_emergencia")));
        query.skip((long) filtro.pagina() * filtro.tamano());
        query.limit(filtro.tamano());

        List<EmergenciaVial> contenido = mongoTemplate
                .find(query, EmergenciaMongoDocument.class, EmergenciaMongoDocument.COLLECTION)
                .stream()
                .map(EmergenciaMongoMapper::toDomain)
                .toList();

        int paginas = (int) Math.ceil((double) total / filtro.tamano());
        return new PaginaResultado<>(contenido, filtro.pagina(), filtro.tamano(), total, paginas);
    }

    @Override
    public Optional<EmergenciaVial> obtenerPorId(String idEmergencia) {
        Query query = Query.query(Criteria.where("id_emergencia").is(idEmergencia));
        return Optional.ofNullable(mongoTemplate.findOne(
                query, EmergenciaMongoDocument.class, EmergenciaMongoDocument.COLLECTION))
                .map(EmergenciaMongoMapper::toDomain);
    }

    @Override
    public ResumenIndicadores consultarResumen(FiltroIndicadores filtro) {
        List<Criteria> condiciones = criteriosFechas(filtro.desde(), filtro.hasta());
        agregarTexto(condiciones, "departamento", filtro.departamento());
        agregarTexto(condiciones, "estado_actual", filtro.estado());
        agregarTexto(condiciones, "evento", filtro.evento());
        Query query = crearConsulta(condiciones);
        long total = mongoTemplate.count(query, EmergenciaMongoDocument.class);
        Criteria conFecha = Criteria.where("fecha_ocurrencia").ne(null);
        EmergenciaMongoDocument primera = mongoTemplate.findOne(
                Query.query(conFecha).with(Sort.by(Sort.Direction.ASC, "fecha_ocurrencia")),
                EmergenciaMongoDocument.class, EmergenciaMongoDocument.COLLECTION);
        EmergenciaMongoDocument ultima = mongoTemplate.findOne(
                Query.query(conFecha).with(Sort.by(Sort.Direction.DESC, "fecha_ocurrencia")),
                EmergenciaMongoDocument.class, EmergenciaMongoDocument.COLLECTION);

        return new ResumenIndicadores(
                filtro.desde(),
                filtro.hasta(),
                aFechaLocal(primera),
                aFechaLocal(ultima),
                total,
                agrupar(condiciones, "estado_actual"),
                agrupar(condiciones, "departamento"),
                agrupar(condiciones, "evento"));
    }

    private LocalDate aFechaLocal(EmergenciaMongoDocument documento) {
        return documento == null || documento.fechaOcurrencia() == null
                ? null
                : Instant.ofEpochMilli(documento.fechaOcurrencia().getTime()).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private List<Criteria> filtros(FiltroEmergencias filtro) {
        List<Criteria> condiciones = criteriosFechas(filtro.desde(), filtro.hasta());
        agregarTexto(condiciones, "departamento", filtro.departamento());
        agregarTexto(condiciones, "provincia", filtro.provincia());
        agregarTexto(condiciones, "distrito", filtro.distrito());
        agregarTexto(condiciones, "estado_actual", filtro.estado());
        agregarTexto(condiciones, "evento", filtro.evento());
        agregarTexto(condiciones, "red_afectada", filtro.red());
        agregarBusqueda(condiciones, filtro.busqueda());
        return condiciones;
    }

    private List<Criteria> criteriosFechas(LocalDate desde, LocalDate hasta) {
        List<Criteria> condiciones = new ArrayList<>();
        if (desde == null && hasta == null) {
            return condiciones;
        }

        Criteria fecha = Criteria.where("fecha_ocurrencia");
        if (desde != null) {
            fecha.gte(desde.atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        if (hasta != null) {
            fecha.lt(hasta.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        condiciones.add(fecha);
        return condiciones;
    }

    private void agregarTexto(List<Criteria> condiciones, String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        String exactoIgnorandoMayusculas = "^" + Pattern.quote(valor.trim()) + "$";
        condiciones.add(Criteria.where(campo).regex(exactoIgnorandoMayusculas, "i"));
    }

    private void agregarBusqueda(List<Criteria> condiciones, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        String termino = Pattern.quote(valor.trim());
        Criteria[] campos = {
                Criteria.where("id_emergencia").regex(termino, "i"),
                Criteria.where("evento").regex(termino, "i"),
                Criteria.where("departamento").regex(termino, "i"),
                Criteria.where("provincia").regex(termino, "i"),
                Criteria.where("distrito").regex(termino, "i"),
                Criteria.where("red_afectada").regex(termino, "i"),
                Criteria.where("tramo_afectado").regex(termino, "i"),
                Criteria.where("sector_afectado").regex(termino, "i"),
                Criteria.where("hechos").regex(termino, "i"),
                Criteria.where("novedades").regex(termino, "i")
        };
        condiciones.add(new Criteria().orOperator(campos));
    }

    private Query crearConsulta(List<Criteria> condiciones) {
        if (condiciones.isEmpty()) {
            return new Query();
        }
        return Query.query(new Criteria().andOperator(condiciones.toArray(Criteria[]::new)));
    }

    private List<IndicadorItem> agrupar(List<Criteria> condiciones, String campo) {
        List<AggregationOperation> operaciones = new ArrayList<>();
        if (!condiciones.isEmpty()) {
            operaciones.add(Aggregation.match(new Criteria().andOperator(condiciones.toArray(Criteria[]::new))));
        }
        operaciones.add(Aggregation.group(campo).count().as("cantidad"));
        operaciones.add(Aggregation.project().and("_id").as("categoria").and("cantidad").as("cantidad"));
        operaciones.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "cantidad")));

        Aggregation aggregation = Aggregation.newAggregation(operaciones);
        AggregationResults<Document> resultados = mongoTemplate.aggregate(
                aggregation, EmergenciaMongoDocument.COLLECTION, Document.class);

        return resultados.getMappedResults().stream()
                .map(document -> new IndicadorItem(
                        document.getString("categoria"),
                        ((Number) document.get("cantidad")).longValue()))
                .toList();
    }
}
