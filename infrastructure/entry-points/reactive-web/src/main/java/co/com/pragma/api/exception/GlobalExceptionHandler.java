package co.com.pragma.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validacion fallida");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, Object>> handleServerWebInput(ServerWebInputException ex) {
        Throwable rootCause = ex.getCause();
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Error de entrada de dato");
        body.put("message", rootCause != null ? rootCause.getClass().getSimpleName() : ex.getClass().getSimpleName());
        body.put("detail", rootCause != null ? rootCause.getMessage() : ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(com.fasterxml.jackson.core.JsonProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonProcessing(com.fasterxml.jackson.core.JsonProcessingException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error al procesar JSON");
        body.put("message", "El JSON enviado tiene un formato inválido.");
        body.put("detail", ex.getOriginalMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validacion fallida");
        response.put("message", ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        response.put("timestamp", LocalDateTime.now());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Error de negocio");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    @ExceptionHandler(NullPointerException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNullPointerException(NullPointerException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Error interno - dato nulo inesperado");
        response.put("message", "Ocurrio un error porque un dato requerido esta vacio o no fue inicializado.");
        response.put("detail", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception ex) {
        Throwable rootCause = ex.getCause();
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Error inesperado");
        response.put("message", rootCause != null ? rootCause.getClass().getSimpleName() : ex.getClass().getSimpleName());
        response.put("detail", rootCause != null ? rootCause.getMessage() : ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

}