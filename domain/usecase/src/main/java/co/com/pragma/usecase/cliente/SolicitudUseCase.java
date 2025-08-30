package co.com.pragma.usecase.cliente;

import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SQSPublisher;
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
    private final EstadoRepository estadoRepository;
    private final SQSPublisher sqsPublisher;

    /**
     * Crear una nueva solicitud de crédito
     */
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

    /**
     * Listar solicitudes por estado
     */
    @Override
    public Flux<Solicitud> listarSolicitudesPorEstado(Long idEstado) {
        if (idEstado == null || idEstado <= 0) {
            return Flux.error(new IllegalArgumentException("El idEstado debe ser un valor positivo"));
        }
        return solicitudRepository.obtenerSolicitudesPorEstado(idEstado);
    }

    /**
     * Cambiar estado de la solicitud
     */
    @Override
    public Mono<Void> cambiarEstadoSolicitud(Long idSolicitud, Long idEstado) {
        return validarEntradas(idSolicitud, idEstado)
                .then(validarEstadoExiste(idEstado))
                .then(solicitudRepository.findByIdSolicitud(idSolicitud)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("La solicitud no existe")))
                        .flatMap(solicitud -> actualizarEstado(idSolicitud, idEstado)
                                .then(enviarMensajeSiCorresponde(solicitud, idEstado))
                        )
                )
                .then();
    }

    /**
     * Enviar mensaje a SQS si corresponde (aprobada o rechazada)
     */
    private Mono<Void> enviarMensajeSiCorresponde(Solicitud solicitud, Long idEstado) {
        if (idEstado == 2L || idEstado == 4L) {
            String estadoStr = (idEstado == 2L) ? "APROBADA" : "RECHAZADA";

            String mensaje = String.format(
                    "{ \"idSolicitud\": %d, \"email\": \"%s\", \"nombre\": \"%s\", \"estado\": \"%s\", \"monto\": %s }",
                    solicitud.getIdSolicitud(),
                    solicitud.getEmail(),
                    solicitud.getNombre(),
                    estadoStr,
                    solicitud.getMonto()
            );

            return sqsPublisher.enviarMensaje(mensaje)
                    .onErrorResume(e -> {
                        // Loguear error, pero no romper flujo
                        System.err.println("Error enviando mensaje a SQS: " + e.getMessage());
                        return Mono.empty();
                    });
        }
        return Mono.empty();
    }

    /**
     * Validar que el solicitante exista en el microservicio de solicitantes
     */
    private Mono<Void> validarExistenciaSolicitante(String documentoIdentidad) {
        return solicitanteConsumerGateway.verificarExistenciaSolicitante(documentoIdentidad)
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new IllegalArgumentException(
                                "El solicitante con documento " + documentoIdentidad + " no existe. " +
                                        "Debe registrarse primero antes de crear una solicitud."
                        ));
                    }
                });
    }

    /**
     * Validar que el tipo de préstamo exista en BD
     */
    private Mono<Void> validarTipoPrestamo(Long idTipoPrestamo) {
        return tipoPrestamoRepository.findByIdTipoPrestamo(idTipoPrestamo)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "El tipo de préstamo con ID " + idTipoPrestamo + " no existe"
                )))
                .then();
    }

    /**
     * Aplica cálculos y asigna valores antes de guardar
     */
    private Mono<Solicitud> prepararSolicitudParaGuardar(Solicitud solicitud) {
        aplicarDeudaYEstado(solicitud);
        return asignarSolicitudesAprobadas(solicitud);
    }

    /**
     * Calcula deudaMensual y asigna estado inicial (Pendiente)
     */
    private void aplicarDeudaYEstado(Solicitud solicitud) {
        BigDecimal deudaMensual = solicitud.getMonto()
                .multiply(solicitud.getTasaInteres())
                .divide(BigDecimal.valueOf(solicitud.getPlazo()), RoundingMode.HALF_UP);

        solicitud.setDeudaMensual(deudaMensual);
        solicitud.setIdEstado(1L); // Estado "Pendiente"
    }

    /**
     * Consulta en BD cuántas solicitudes aprobadas tiene el solicitante y las asigna
     */
    private Mono<Solicitud> asignarSolicitudesAprobadas(Solicitud solicitud) {
        return solicitudRepository.contarSolicitudesAprobadasPorDocumento(
                        solicitud.getDocumentoIdentidad(),
                        2L // Estado aprobado
                )
                .map(count -> {
                    solicitud.setSolicitudesAprobadas(count);
                    return solicitud;
                });
    }

    /**
     * Validar idSolicitud e idEstado
     */
    private Mono<Void> validarEntradas(Long idSolicitud, Long idEstado) {
        if (idSolicitud == null || idSolicitud <= 0) {
            return Mono.error(new IllegalArgumentException("El idSolicitud debe ser válido"));
        }
        if (idEstado == null || idEstado <= 0) {
            return Mono.error(new IllegalArgumentException("El idEstado debe ser válido"));
        }
        return Mono.empty();
    }

    /**
     * Validar que el estado exista en BD
     */
    private Mono<Void> validarEstadoExiste(Long idEstado) {
        return estadoRepository.findByidEstado(idEstado)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El estado no existe")))
                .then();
    }

    /**
     * Actualizar el estado de la solicitud en BD
     */
    private Mono<Void> actualizarEstado(Long idSolicitud, Long idEstado) {
        return solicitudRepository.actualizarEstadoSolicitud(idSolicitud, idEstado)
                .flatMap(rows -> {
                    if (rows == 0) {
                        return Mono.error(new IllegalStateException(
                                "No se pudo actualizar la solicitud con id " + idSolicitud +
                                        " (no existe o ya tenía el estado " + idEstado + ")"));
                    }
                    return Mono.empty();
                });
    }
}
