package pe.inteligenciapa.application.service;

public class EmergenciaNoEncontradaException extends RuntimeException {
    public EmergenciaNoEncontradaException(String idEmergencia) {
        super("No se encontró la emergencia con identificador " + idEmergencia);
    }
}
