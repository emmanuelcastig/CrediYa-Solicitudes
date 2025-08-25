package co.com.pragma.usecase.cliente.in;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface CrearSolicitudCredito {
    Mono<Solicitud> crearSolicitud(Solicitud solicitud);
}
