package co.com.pragma.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_DeberiaRetornarBadRequest() {
        ValidationException ex = new ValidationException("Error de validación");

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleValidationException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Validacion fallida", response.getBody().get("error"));
                    assertEquals("Error de validación", response.getBody().get("message"));
                })
                .verifyComplete();
    }

    @Test
    void handleWebClientResponseException_DeberiaRetornarErrorServicioExterno() {
        WebClientResponseException ex = new WebClientResponseException(
                "Service Down",
                502,
                "Bad Gateway",
                null,
                "Detalle del error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleWebClientResponseException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(502, response.getBody().get("status"));
                    assertEquals("Error en servicio externo", response.getBody().get("error"));
                    assertEquals("Bad Gateway", response.getBody().get("message"));
                    assertEquals("Detalle del error", response.getBody().get("detail"));
                    assertEquals("SERVICE_ERROR", response.getBody().get("code"));
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
        assertEquals("Error de entrada de dato", response.getBody().get("error"));
        assertEquals("IllegalArgumentException", response.getBody().get("message"));
        assertEquals("Mensaje de error", response.getBody().get("detail"));
    }

    @Test
    void handleJsonProcessingException_DeberiaRetornarBadRequest() {
        JsonProcessingException ex = mock(JsonProcessingException.class);
        when(ex.getOriginalMessage()).thenReturn("JSON inválido");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleJsonProcessing(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al procesar JSON", response.getBody().get("error"));
        assertEquals("El JSON enviado tiene un formato inválido.", response.getBody().get("message"));
        assertEquals("JSON inválido", response.getBody().get("detail"));
    }

    @Test
    void handleWebExchangeBindException_DeberiaRetornarErroresDeCampos() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        var fieldError = new org.springframework.validation.FieldError("obj", "campo",
                "es obligatorio");
        when(ex.getFieldErrors()).thenReturn(List.of(fieldError));

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleWebExchangeBindException(ex);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Validacion fallida", response.getBody().get("error"));
                    assertTrue(((List<?>) response.getBody().get("message")).get(0).toString()
                            .contains("campo: es obligatorio"));
                })
                .verifyComplete();
    }

    @Test
    void handleIllegalArgumentException_DeberiaRetornarConflict() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        StepVerifier.create(exceptionHandler.handleIllegalArgument(ex))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertEquals("Error de negocio", response.getBody().get("error"));
                    assertEquals("Argumento inválido", response.getBody().get("message"));
                })
                .verifyComplete();
    }

    @Test
    void handleNullPointerException_DeberiaRetornarInternalServerError() {
        NullPointerException ex = new NullPointerException("Objeto nulo");

        StepVerifier.create(exceptionHandler.handleNullPointerException(ex))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("Error interno - dato nulo inesperado", response.getBody().get("error"));
                    assertEquals("Objeto nulo", response.getBody().get("detail"));
                })
                .verifyComplete();
    }

    @Test
    void handleGeneralException_DeberiaRetornarInternalServerError() {
        Exception ex = new RuntimeException("Error general");

        StepVerifier.create(exceptionHandler.handleGeneralException(ex))
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals("Error inesperado", response.getBody().get("error"));
                    assertEquals("RuntimeException", response.getBody().get("message"));
                    assertEquals("Error general", response.getBody().get("detail"));
                })
                .verifyComplete();
    }

    @Test
    void handleGeneralException_ConCausa_DeberiaRetornarInfoDeCausa() {
        Exception rootCause = new IllegalArgumentException("Causa raíz");
        Exception ex = new RuntimeException("Error general", rootCause);

        StepVerifier.create(exceptionHandler.handleGeneralException(ex))
                .assertNext(response -> {
                    assertEquals("IllegalArgumentException", response.getBody().get("message"));
                    assertEquals("Causa raíz", response.getBody().get("detail"));
                })
                .verifyComplete();
    }
}
