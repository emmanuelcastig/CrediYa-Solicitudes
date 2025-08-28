package co.com.pragma.usecase.cliente.in;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CrearSolicitudCredito {
    Mono<Solicitud> crearSolicitud(Solicitud solicitud);
    Flux<Solicitud> listarSolicitudesPorEstado(Long idEstado);
    Mono<Void> cambiarEstadoSolicitud(Long idSolicitud, Long idEstado);
}
