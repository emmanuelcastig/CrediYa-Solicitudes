package co.com.pragma.model.solicitud.gateways;

import reactor.core.publisher.Mono;

public interface SQSPublisher {
    Mono<Void> enviarMensaje(String mensaje);
}
