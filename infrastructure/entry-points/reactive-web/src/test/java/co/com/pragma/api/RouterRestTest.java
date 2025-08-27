package co.com.pragma.api;

import co.com.pragma.api.dto.SolicitudRequest;
import co.com.pragma.api.dto.SolicitudResponse;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CREATED;

@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private CrearSolicitudCredito crearSolicitudCredito;

    @Mock
    private Validator validator;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private SolicitudMapper solicitudMapper;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private Handler handler;

    private SolicitudRequest solicitudRequest;
    private Solicitud solicitudDomain;
    private SolicitudResponse solicitudResponse;

    @BeforeEach
    void setUp() {
        solicitudRequest = new SolicitudRequest();
        solicitudRequest.setEmail("test@correo.com");

        solicitudDomain = new Solicitud();
        solicitudResponse = new SolicitudResponse();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("token", "fake-token");
        attrs.put("email", "test@correo.com");
        attrs.put("rol", "CLIENTE");

        when(serverRequest.attributes()).thenReturn(attrs);
    }
    @Test
    void crearSolicitud_DeberiaRetornarCreatedCuandoTodoEsExitoso() {
        when(serverRequest.bodyToMono(SolicitudRequest.class)).thenReturn(Mono.just(solicitudRequest));
        when(validator.validate(solicitudRequest)).thenReturn(Collections.emptySet());
        when(solicitudMapper.toDomain(solicitudRequest)).thenReturn(solicitudDomain);
        when(crearSolicitudCredito.crearSolicitud(solicitudDomain)).thenReturn(Mono.just(solicitudDomain));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(solicitudMapper.toResponse(solicitudDomain)).thenReturn(solicitudResponse);

        StepVerifier.create(handler.crearSolicitud(serverRequest))
                .expectNextMatches(serverResponse -> serverResponse.statusCode() == CREATED)
                .verifyComplete();

        verify(validator).validate(solicitudRequest);
        verify(solicitudMapper).toDomain(solicitudRequest);
        verify(crearSolicitudCredito).crearSolicitud(solicitudDomain);
        verify(solicitudMapper).toResponse(solicitudDomain);
        verify(transactionalOperator).transactional(any(Mono.class));
    }

    @Test
    void crearSolicitud_DeberiaManejarValidacionFallida() {
        ConstraintViolation<SolicitudRequest> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(serverRequest.bodyToMono(SolicitudRequest.class)).thenReturn(Mono.just(solicitudRequest));
        when(path.toString()).thenReturn("campo");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("no puede ser nulo");
        when(validator.validate(solicitudRequest)).thenReturn(Set.of(violation));

        StepVerifier.create(handler.crearSolicitud(serverRequest))
                .expectError(ValidationException.class)
                .verify();

        verify(validator).validate(solicitudRequest);
        verifyNoInteractions(solicitudMapper, crearSolicitudCredito, transactionalOperator);
    }

    @Test
    void crearSolicitud_DeberiaManejarErrorEnCasoDeUso() {
        when(serverRequest.bodyToMono(SolicitudRequest.class)).thenReturn(Mono.just(solicitudRequest));
        when(validator.validate(solicitudRequest)).thenReturn(Collections.emptySet());
        when(solicitudMapper.toDomain(solicitudRequest)).thenReturn(solicitudDomain);
        when(crearSolicitudCredito.crearSolicitud(solicitudDomain))
                .thenReturn(Mono.error(new RuntimeException("Error en base de datos")));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(handler.crearSolicitud(serverRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(validator).validate(solicitudRequest);
        verify(solicitudMapper).toDomain(solicitudRequest);
        verify(crearSolicitudCredito).crearSolicitud(solicitudDomain);
        verify(transactionalOperator).transactional(any(Mono.class));
        verify(solicitudMapper, never()).toResponse(any());
    }

    @Test
    void crearSolicitud_DeberiaManejarErrorEnMapeo() {
        when(serverRequest.bodyToMono(SolicitudRequest.class)).thenReturn(Mono.just(solicitudRequest));
        when(validator.validate(solicitudRequest)).thenReturn(Collections.emptySet());
        when(solicitudMapper.toDomain(solicitudRequest)).thenThrow(new RuntimeException("Error en mapeo"));

        StepVerifier.create(handler.crearSolicitud(serverRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(validator).validate(solicitudRequest);
        verify(solicitudMapper).toDomain(solicitudRequest);
        verifyNoInteractions(crearSolicitudCredito, transactionalOperator);
    }

    @Test
    void crearSolicitud_DeberiaManejarMapperToResponseNull() {
        when(serverRequest.bodyToMono(SolicitudRequest.class)).thenReturn(Mono.just(solicitudRequest));
        when(validator.validate(solicitudRequest)).thenReturn(Collections.emptySet());
        when(solicitudMapper.toDomain(solicitudRequest)).thenReturn(solicitudDomain);
        when(crearSolicitudCredito.crearSolicitud(solicitudDomain)).thenReturn(Mono.just(solicitudDomain));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(solicitudMapper.toResponse(solicitudDomain)).thenReturn(null); // ← Response null

        StepVerifier.create(handler.crearSolicitud(serverRequest))
                .expectError(NullPointerException.class)
                .verify();
    }
}
