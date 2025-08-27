package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.EstadoEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EstadoReactiveRepository extends ReactiveCrudRepository<EstadoEntity, Long>
        , ReactiveQueryByExampleExecutor<EstadoEntity> {
    Mono<EstadoEntity> findByIdEstado(Long idEstado);
}