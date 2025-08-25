package co.com.pragma.usecase.cliente;

import co.com.pragma.model.enums.Estado;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase implements CrearSolicitudCredito {
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final SolicitudRepository solicitudRepository;
    private final SolicitanteConsumerGateway solicitanteConsumerGateway;


    @Override
    public Mono<Solicitud> crearSolicitud(Solicitud solicitud) {
        return Mono.just(solicitud)
                .flatMap(sol -> validarExistenciaSolicitante(sol.getDocumentoIdentidad())
                        .thenReturn(sol))
                .flatMap(sol -> validarTipoPrestamo(sol.getIdTipoPrestamo())
                        .thenReturn(sol))
                .doOnNext(sol -> sol.setEstado(Estado.PENDIENTE))
                .flatMap(solicitudRepository::guardarSolicitud);
    }

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

    private Mono<Void> validarTipoPrestamo(Long idTipoPrestamo) {
        return tipoPrestamoRepository.findByIdTipoPrestamo(idTipoPrestamo)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "El tipo de préstamo con ID " + idTipoPrestamo + " no existe")))
                .then();
    }
}

