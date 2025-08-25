package co.com.pragma.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_DeberiaRetornarBadRequest() {

        ValidationException ex = new ValidationException("Error de validación");

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleValidationException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
                    assertEquals("Validacion fallida", response.getBody().get("error"));
                    assertEquals("Error de validación", response.getBody().get("message"));
                    assertNotNull(response.getBody().get("timestamp"));
                })
                .verifyComplete();
    }

    @Test
    void handleServerWebInputException_DeberiaRetornarBadRequest() {

        ServerWebInputException ex = mock(ServerWebInputException.class);
        when(ex.getCause()).thenReturn(new IllegalArgumentException("Mensaje de error"));
        when(ex.getMessage()).thenReturn("Error de entrada");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleServerWebInput(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Error de entrada de dato", response.getBody().get("error"));
        assertEquals("IllegalArgumentException", response.getBody().get("message"));
        assertEquals("Mensaje de error", response.getBody().get("detail"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleJsonProcessingException_DeberiaRetornarBadRequest() {

        JsonProcessingException ex = mock(JsonProcessingException.class);
        when(ex.getOriginalMessage()).thenReturn("JSON inválido");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleJsonProcessing(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Error al procesar JSON", response.getBody().get("error"));
        assertEquals("El JSON enviado tiene un formato inválido.", response.getBody().get("message"));
        assertEquals("JSON inválido", response.getBody().get("detail"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleWebExchangeBindException_DeberiaRetornarBadRequest() {

        WebExchangeBindException ex = mock(WebExchangeBindException.class);

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleWebExchangeBindException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
                    assertEquals("Validacion fallida", response.getBody().get("error"));
                    assertNotNull(response.getBody().get("message"));
                    assertNotNull(response.getBody().get("timestamp"));
                })
                .verifyComplete();
    }

    @Test
    void handleIllegalArgumentException_DeberiaRetornarConflict() {

        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleIllegalArgument(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(HttpStatus.CONFLICT.value(), response.getBody().get("status"));
                    assertEquals("Error de negocio", response.getBody().get("error"));
                    assertEquals("Argumento inválido", response.getBody().get("message"));
                    assertNotNull(response.getBody().get("timestamp"));
                })
                .verifyComplete();
    }

    @Test
    void handleNullPointerException_DeberiaRetornarInternalServerError() {

        NullPointerException ex = new NullPointerException("Objeto nulo");

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleNullPointerException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
                    assertEquals("Error interno - dato nulo inesperado", response.getBody().get("error"));
                    assertEquals("Ocurrio un error porque un dato requerido esta vacio o no fue inicializado.", response.getBody().get("message"));
                    assertEquals("Objeto nulo", response.getBody().get("detail"));
                    assertNotNull(response.getBody().get("timestamp"));
                })
                .verifyComplete();
    }

    @Test
    void handleGeneralException_DeberiaRetornarInternalServerError() {

        Exception ex = new RuntimeException("Error general");

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleGeneralException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
                    assertEquals("Error inesperado", response.getBody().get("error"));
                    assertEquals("RuntimeException", response.getBody().get("message"));
                    assertEquals("Error general", response.getBody().get("detail"));
                    assertNotNull(response.getBody().get("timestamp"));
                })
                .verifyComplete();
    }

    @Test
    void handleGeneralException_ConCausa_DeberiaRetornarInfoDeCausa() {

        Exception rootCause = new IllegalArgumentException("Causa raíz");
        Exception ex = new RuntimeException("Error general", rootCause);

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleGeneralException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals("IllegalArgumentException", response.getBody().get("message"));
                    assertEquals("Causa raíz", response.getBody().get("detail"));
                })
                .verifyComplete();
    }

}