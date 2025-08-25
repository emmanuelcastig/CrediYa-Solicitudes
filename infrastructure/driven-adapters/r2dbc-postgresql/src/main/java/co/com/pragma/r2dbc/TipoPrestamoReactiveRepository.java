package co.com.pragma.r2dbc;


import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TipoPrestamoReactiveRepository extends ReactiveCrudRepository<TipoPrestamoEntity, Long>
        , ReactiveQueryByExampleExecutor<TipoPrestamoEntity> {
    Mono<TipoPrestamoEntity> findByIdTipoPrestamo(Long idTipoPrestamo);
}