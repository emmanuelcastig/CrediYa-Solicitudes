package co.com.pragma.usecase.cliente;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

class SolicitudUseCaseTest {

    private TipoPrestamoRepository tipoPrestamoRepository;
    private SolicitudRepository solicitudRepository;
    private SolicitanteConsumerGateway solicitanteConsumerGateway;
    private SolicitudUseCase solicitudUseCase;

    @BeforeEach
    void setUp() {
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        solicitanteConsumerGateway = Mockito.mock(SolicitanteConsumerGateway.class);
        solicitudUseCase = new SolicitudUseCase(tipoPrestamoRepository, solicitudRepository, solicitanteConsumerGateway);
    }

    @Test
    void crearSolicitud_exito() {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdTipoPrestamo(1L);
        solicitud.setDocumentoIdentidad("123");
        solicitud.setMonto(BigDecimal.valueOf(1000));
        solicitud.setPlazo(10);
        solicitud.setTasaInteres(BigDecimal.valueOf(0.05));

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setIdTipoPrestamo(1L);

        Mockito.when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        Mockito.when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.just(tipoPrestamo));
        Mockito.when(solicitudRepository.contarSolicitudesAprobadasPorDocumento("123", 2L))
                .thenReturn(Mono.just(3)); // simulamos que tiene 3 solicitudes aprobadas
        Mockito.when(solicitudRepository.guardarSolicitud(Mockito.any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectNextMatches(s ->
                        s.getIdEstado() == 1L &&               // Estado inicial pendiente
                                s.getSolicitudesAprobadas() == 3 &&    // Mock de solicitudes aprobadas
                                s.getDeudaMensual().compareTo(BigDecimal.ZERO) > 0 // Se calculó deudaMensual
                )
                .verifyComplete();
    }

    @Test
    void crearSolicitud_solicitanteNoExiste() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("999");
        solicitud.setIdTipoPrestamo(1L);

        Mockito.when(solicitanteConsumerGateway.verificarExistenciaSolicitante("999"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("El solicitante con documento 999 no existe"))
                .verify();

        Mockito.verify(tipoPrestamoRepository, Mockito.never())
                .findByIdTipoPrestamo(Mockito.anyLong());
        Mockito.verify(solicitudRepository, Mockito.never())
                .guardarSolicitud(Mockito.any());
    }

    @Test
    void crearSolicitud_tipoPrestamoNoExiste() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123");
        solicitud.setIdTipoPrestamo(99L);

        Mockito.when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        Mockito.when(tipoPrestamoRepository.findByIdTipoPrestamo(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El tipo de préstamo con ID 99 no existe"))
                .verify();

        Mockito.verify(solicitudRepository, Mockito.never())
                .guardarSolicitud(Mockito.any());
    }

    @Test
    void crearSolicitud_errorEnRepositorio() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123");
        solicitud.setIdTipoPrestamo(1L);
        solicitud.setMonto(BigDecimal.valueOf(1000));
        solicitud.setPlazo(10);
        solicitud.setTasaInteres(BigDecimal.valueOf(0.05));

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setIdTipoPrestamo(1L);

        Mockito.when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        Mockito.when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.just(tipoPrestamo));
        Mockito.when(solicitudRepository.contarSolicitudesAprobadasPorDocumento("123", 2L))
                .thenReturn(Mono.just(1));
        Mockito.when(solicitudRepository.guardarSolicitud(Mockito.any()))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("DB error"))
                .verify();
    }
}
