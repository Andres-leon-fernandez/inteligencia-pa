package pe.inteligenciapa.adapter.out.mongodb.document;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = EmergenciaMongoDocument.COLLECTION)
public record EmergenciaMongoDocument(
        @Id String mongoId,
        @Field("id_emergencia") String idEmergencia,
        @Field("codigo_reporte") String codigoReporte,
        @Field("fecha_ocurrencia") Date fechaOcurrencia,
        @Field("clase_fenomeno") String claseFenomeno,
        @Field("fenomeno") String fenomeno,
        @Field("evento") String evento,
        @Field("departamento") String departamento,
        @Field("provincia") String provincia,
        @Field("distrito") String distrito,
        @Field("afectacion") String afectacion,
        @Field("red_afectada") String redAfectada,
        @Field("tramo_afectado") String tramoAfectado,
        @Field("sector_afectado") String sectorAfectado,
        @Field("hechos") String hechos,
        @Field("novedades") String novedades,
        @Field("fuente") String fuente,
        @Field("estado_inicial") String estadoInicial,
        @Field("fecha_cierre") Date fechaCierre,
        @Field("estado_actual") String estadoActual,
        @Field("latitud") Double latitud,
        @Field("longitud") Double longitud,
        @Field("fecha_corte") Date fechaCorte) {

    public static final String COLLECTION = "emergencias_viales";
}
