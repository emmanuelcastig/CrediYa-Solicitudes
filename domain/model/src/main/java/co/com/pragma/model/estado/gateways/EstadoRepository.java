package co.com.pragma.model.estado.gateways;

import co.com.pragma.model.estado.Estado;
import reactor.core.publisher.Mono;

public interface EstadoRepository {
    Mono<Estado> findByidEstado(Long idEstado);
}
