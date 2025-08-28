package co.com.pragma.usecase.cliente;

import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
public class SolicitudUseCase implements CrearSolicitudCredito {
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final SolicitudRepository solicitudRepository;
    private final SolicitanteConsumerGateway solicitanteConsumerGateway;


    @Override
    public Mono<Solicitud> crearSolicitud(Solicitud solicitud) {
        return Mono.just(solicitud)
                // 1. Validar que el solicitante exista
                .flatMap(sol -> validarExistenciaSolicitante(sol.getDocumentoIdentidad())
                        .thenReturn(sol))
                // 2. Validar que el tipo de préstamo exista
                .flatMap(sol -> validarTipoPrestamo(sol.getIdTipoPrestamo())
                        .thenReturn(sol))
                // 3. Enriquecer la solicitud con cálculos antes de guardar
                .flatMap(this::prepararSolicitudParaGuardar)
                // 4. Guardar la solicitud
                .flatMap(solicitudRepository::guardarSolicitud);
    }

    // Listar solicitudes por estado recibido desde la petición
    @Override
    public Flux<Solicitud> listarSolicitudesPorEstado(Long idEstado) {
        if (idEstado == null || idEstado <= 0) {
            return Flux.error(new IllegalArgumentException("El idEstado debe ser un valor positivo"));
        }
        return solicitudRepository.obtenerSolicitudesPorEstado(idEstado);
    }

    //Validar que el solicitante exista en el microservicio de solicitantes
    private Mono<Void> validarExistenciaSolicitante(String documentoIdentidad) {
        return solicitanteConsumerGateway.verificarExistenciaSolicitante(documentoIdentidad)
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new IllegalArgumentException(
                                "El solicitante con documento " + documentoIdentidad + " no existe. " +
                                        "Debe registrarse primero antes de crear una solicitud."));
                    }
                });
    }

    //Validar que el tipo de préstamo exista en BD
    private Mono<Void> validarTipoPrestamo(Long idTipoPrestamo) {
        return tipoPrestamoRepository.findByIdTipoPrestamo(idTipoPrestamo)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "El tipo de préstamo con ID " + idTipoPrestamo + " no existe")))
                .then();
    }


    // aplica todos los cálculos
    private Mono<Solicitud> prepararSolicitudParaGuardar(Solicitud solicitud) {
        aplicarDeudaYEstado(solicitud);
        return asignarSolicitudesAprobadas(solicitud);
    }


    // Calcula deudaMensual y asigna estado inicial.
    private void aplicarDeudaYEstado(Solicitud solicitud) {
        BigDecimal deudaMensual = solicitud.getMonto()
                .multiply(solicitud.getTasaInteres())
                .divide(BigDecimal.valueOf(solicitud.getPlazo()), RoundingMode.HALF_UP);

        solicitud.setDeudaMensual(deudaMensual);
        solicitud.setIdEstado(1L); // Estado "Pendiente"
    }

    /**
     * Consulta en BD cuántas solicitudes aprobadas tiene el solicitante
     * y lo asigna al objeto.
     */
    private Mono<Solicitud> asignarSolicitudesAprobadas(Solicitud solicitud) {
        return solicitudRepository.contarSolicitudesAprobadasPorDocumento(
                        solicitud.getDocumentoIdentidad(), 2L // Estado aprobado
                )
                .map(count -> {
                    solicitud.setSolicitudesAprobadas(count);
                    return solicitud;
                });
    }
}