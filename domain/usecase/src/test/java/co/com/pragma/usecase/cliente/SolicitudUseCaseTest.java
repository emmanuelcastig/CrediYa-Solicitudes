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
        solicitud.setSalarioBase(BigDecimal.valueOf(5000));
        return solicitud;
    }

    @Test
    void crearSolicitud_exito_conValidacionAutomatica() {
        Solicitud solicitud = buildSolicitudValida();

        TipoPrestamo tipoPrestamo = new TipoPrestamo();
        tipoPrestamo.setIdTipoPrestamo(1L);
        tipoPrestamo.setValidacionAutomatica(true);

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.just(tipoPrestamo));
        when(solicitudRepository.contarSolicitudesAprobadasPorDocumento("123", 2L))
                .thenReturn(Mono.just(3));
        when(solicitudRepository.guardarSolicitud(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(solicitudRepository.sumarCuotasMensualesEnSolicitudesAprobadas("123", 2L))
                .thenReturn(Mono.just(BigDecimal.valueOf(100)));
        when(sqsPublisher.enviarMensajeValidacionAutomatica(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectNextMatches(s ->
                        s.getIdEstado() == 1L &&
                                s.getSolicitudesAprobadas() == 3 &&
                                s.getDeudaMensual().compareTo(BigDecimal.ZERO) > 0
                )
                .verifyComplete();

        verify(sqsPublisher).enviarMensajeValidacionAutomatica(anyString());
    }

    @Test
    void crearSolicitud_solicitanteNoExiste() {
        Solicitud solicitud = buildSolicitudValida();

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("no existe"))
                .verify();

        verifyNoInteractions(tipoPrestamoRepository);
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crearSolicitud_tipoPrestamoNoExiste() {
        Solicitud solicitud = buildSolicitudValida();

        when(solicitanteConsumerGateway.verificarExistenciaSolicitante("123"))
                .thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findByIdTipoPrestamo(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.crearSolicitud(solicitud))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("no existe"))
                .verify();

        verify(solicitudRepository, never()).guardarSolicitud(any());
    }

    @Test
    void listarSolicitudesPorEstado_exito() {
        Solicitud solicitud = buildSolicitudValida();

        when(solicitudRepository.obtenerSolicitudesPorEstado(2L))
                .thenReturn(Flux.just(solicitud));

        StepVerifier.create(solicitudUseCase.listarSolicitudesPorEstado(2L))
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    void listarSolicitudesPorEstado_errorParametroInvalido() {
        StepVerifier.create(solicitudUseCase.listarSolicitudesPorEstado(0L))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void cambiarEstadoSolicitud_aprobada_enviaSqs() {
        Solicitud solicitud = buildSolicitudValida();

        when(estadoRepository.findByidEstado(2L))
                .thenReturn(Mono.just(Estado.builder().idEstado(2L).nombre("APROBADA").build()));
        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));
        when(solicitudRepository.actualizarEstadoSolicitud(10L, 2L))
                .thenReturn(Mono.just(1));
        when(sqsPublisher.enviarMensajeNotificacion(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 2L))
                .verifyComplete();

        verify(sqsPublisher).enviarMensajeNotificacion(anyString());
    }

    @Test
    void cambiarEstadoSolicitud_estadoNoExiste() {
        Solicitud solicitud = buildSolicitudValida();

        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));
        when(estadoRepository.findByidEstado(99L)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 99L))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("estado no existe"))
                .verify();
    }

    @Test
    void cambiarEstadoSolicitud_noSeActualizo() {
        Solicitud solicitud = buildSolicitudValida();

        when(estadoRepository.findByidEstado(2L))
                .thenReturn(Mono.just(Estado.builder().idEstado(2L).nombre("APROBADA").build()));
        when(solicitudRepository.findByIdSolicitud(10L))
                .thenReturn(Mono.just(solicitud));
        when(solicitudRepository.actualizarEstadoSolicitud(10L, 2L))
                .thenReturn(Mono.just(0));

        StepVerifier.create(solicitudUseCase.cambiarEstadoSolicitud(10L, 2L))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().contains("No se pudo actualizar"))
                .verify();

        verify(sqsPublisher, never()).enviarMensajeNotificacion(anyString());
    }
}
