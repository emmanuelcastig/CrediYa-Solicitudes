package co.com.pragma.r2dbc;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudReactiveRepositoryAdapterTest {
    @Mock
    SolicitudReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @InjectMocks
    SolicitudReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new SolicitudReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void guardarSolicitud_debeGuardarYRetornarSolicitud() {
        Solicitud solicitud = new Solicitud();
        SolicitudEntity entity = new SolicitudEntity();

        when(mapper.map(solicitud, SolicitudEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Solicitud.class)).thenReturn(solicitud);

        Mono<Solicitud> result = adapter.guardarSolicitud(solicitud);

        StepVerifier.create(result)
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void guardarSolicitud_debePropagarErrorCuandoFallaElRepositorio() {
        Solicitud solicitud = new Solicitud();
        SolicitudEntity entity = new SolicitudEntity();
        RuntimeException error = new RuntimeException("Repository error");

        when(mapper.map(solicitud, SolicitudEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(error));

        Mono<Solicitud> result = adapter.guardarSolicitud(solicitud);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Repository error"))
                .verify();
    }
}