package pe.inteligenciapa.infraestructura.adapter.in.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.inteligenciapa.aplicacion.service.EmergenciaNoEncontradaException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(EmergenciaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> noEncontrada(EmergenciaNoEncontradaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", exception.getMessage()));
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, String>> solicitudInvalida(Exception exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensaje",
                        exception.getMessage() == null ? "Parámetros inválidos" : exception.getMessage()));
    }
}
