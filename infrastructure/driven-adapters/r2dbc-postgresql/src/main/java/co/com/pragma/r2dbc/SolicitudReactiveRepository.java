package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.SolicitudEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, Long>
        , ReactiveQueryByExampleExecutor<SolicitudEntity> {
}
