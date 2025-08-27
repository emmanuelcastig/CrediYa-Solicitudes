package co.com.pragma.model.solicitud.gateways;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {
    Mono<Solicitud> guardarSolicitud(Solicitud solicitud);
    Flux<Solicitud> obtenerSolicitudesPorEstado(Long idEstado);
    Mono<Integer> contarSolicitudesAprobadasPorDocumento(String documentoIdentidad, Long idEstado);
}
