package co.com.pragma.model.tipoprestamo.gateways;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> findByIdTipoPrestamo(Long idTipoPrestamo);
}
