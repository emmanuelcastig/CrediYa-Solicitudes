package co.com.pragma.usecase.cliente;

import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SQSPublisher;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudUseCaseTest {

    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private SolicitanteConsumerGateway solicitanteConsumerGateway;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private SQSPublisher sqsPublisher;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud buildSolicitudValida() {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(10L);
        solicitud.setIdTipoPrestamo(1L);
        solicitud.setDocumentoIdentidad("123");
        solicitud.setEmail("test@mail.com");
        solicitud.setNombre("Juan");
        solicitud.setMonto(BigDecimal.valueOf(1000));
        solicitud.setPlazo(10);
        solicitud.setTasaInteres(BigDecimal.valueOf(0.05));
        return solicitud;
    }

    @Test
    void crearSolicitud_exito() {
        Solicitud solicitud = buildSolicitudValida();

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setIdTipoPrestamo(1L);

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.just(tipoPrestamo));
        when(solicitudRepository.contarSolicitudesAprobadasPorDocumento("123", 2L))
                .thenReturn(Mono.just(3));
        when(solicitudRepository.guardarSolicitud(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectNextMatches(s ->
                        s.getIdEstado() == 1L &&
                                s.getSolicitudesAprobadas() == 3 &&
                                s.getDeudaMensual().compareTo(BigDecimal.ZERO) > 0
                )
                .verifyComplete();
    }

    @Test
    void crearSolicitud_solicitanteNoExiste() {
        Solicitud solicitud = buildSolicitudValida();
        solicitud.setDocumentoIdentidad("999");

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("999"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("El solicitante con documento 999 no existe"))
                .verify();

        verify(tipoPrestamoRepository, never()).findByIdTipoPrestamo(anyLong());
        verify(solicitudRepository, never()).guardarSolicitud(any());
    }

    @Test
    void crearSolicitud_tipoPrestamoNoExiste() {
        Solicitud solicitud = buildSolicitudValida();
        solicitud.setIdTipoPrestamo(99L);

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findByIdTipoPrestamo(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El tipo de préstamo con ID 99 no existe"))
                .verify();

        verify(solicitudRepository, never()).guardarSolicitud(any());
    }

    @Test
    void listarSolicitudesPorEstado_exito() {
        Solicitud solicitud = buildSolicitudValida();

        when(solicitudRepository.obtenerSolicitudesPorEstado(1L))
                .thenReturn(Flux.just(solicitud));

        StepVerifier.create(solicitudUseCase.listarSolicitudesPorEstado(1L))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void listarSolicitudesPorEstado_errorParametroInvalido() {
        StepVerifier.create(solicitudUseCase.listarSolicitudesPorEstado(0L))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("idEstado debe ser un valor positivo"))
                .verify();
    }

    @Test
    void cambiarEstadoSolicitud_aprobada_enviaSqs() {
        Solicitud solicitud = buildSolicitudValida();

        when(estadoRepository.findByidEstado(2L))
                .thenReturn(Mono.just(Estado.builder()
                        .idEstado(2L)
                        .nombre("APROBADA")
                        .descripcion("Solicitud aprobada")
                        .build()));
        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));
        when(solicitudRepository.actualizarEstadoSolicitud(10L, 2L))
                .thenReturn(Mono.just(1));
        when(sqsPublisher.enviarMensaje(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 2L))
                .verifyComplete();

        verify(sqsPublisher, times(1)).enviarMensaje(anyString());
    }

    @Test
    void cambiarEstadoSolicitud_estadoNoExiste() {
        // Simular que la solicitud sí existe
        Solicitud solicitud = buildSolicitudValida();
        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));

        // Simular que el estado NO existe
        when(estadoRepository.findByidEstado(99L))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 99L))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("El estado no existe"))
                .verify();
    }

    @Test
    void cambiarEstadoSolicitud_noSeActualizo() {
        Solicitud solicitud = buildSolicitudValida();

        when(estadoRepository.findByidEstado(2L))
                .thenReturn(Mono.just(Estado.builder()
                        .idEstado(2L)
                        .nombre("APROBADA")
                        .descripcion("Solicitud aprobada")
                        .build()));
        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));
        when(solicitudRepository.actualizarEstadoSolicitud(10L, 2L))
                .thenReturn(Mono.just(0));

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 2L))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().contains("No se pudo actualizar"))
                .verify();

        verify(sqsPublisher, never()).enviarMensaje(anyString());
    }

}
