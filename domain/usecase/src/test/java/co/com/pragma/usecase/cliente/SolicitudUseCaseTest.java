package co.com.pragma.usecase.cliente;

import co.com.pragma.model.enums.Estado;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SolicitudUseCaseTest {

    private TipoPrestamoRepository tipoPrestamoRepository;
    private SolicitudRepository solicitudRepository;
    private SolicitudUseCase solicitudUseCase;

    @BeforeEach
    void setUp() {
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        solicitudUseCase = new SolicitudUseCase(tipoPrestamoRepository, solicitudRepository);
    }

    @Test
    void crearSolicitud_exito() {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdTipoPrestamo(1L);

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setIdTipoPrestamo(1L);

        Mockito.when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.just(tipoPrestamo));
        Mockito.when(solicitudRepository.guardarSolicitud(solicitud))
                .thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectNextMatches(s -> s.getEstado() == Estado.PENDIENTE)
                .verifyComplete();
    }

    @Test
    void crearSolicitud_tipoPrestamoNoExiste() {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdTipoPrestamo(99L);

        Mockito.when(tipoPrestamoRepository.findByIdTipoPrestamo(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El tipo de préstamo no existe"))
                .verify();
    }
}