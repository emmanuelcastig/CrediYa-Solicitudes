package co.com.pragma.model.consumer;

import reactor.core.publisher.Mono;

public interface SolicitanteConsumerGateway {
    Mono<Boolean> verificarExistenciaSolicitante(String documentoIdentidad);
}
