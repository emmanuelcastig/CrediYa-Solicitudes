package co.com.pragma.model.solicitud.gateways;

import reactor.core.publisher.Mono;

public interface SQSPublisher {
    Mono<Void> enviarMensajeNotificacion(String mensaje);
    Mono<Void> enviarMensajeValidacionAutomatica(String mensaje);
}
