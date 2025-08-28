    package co.com.pragma.r2dbc;

    import co.com.pragma.r2dbc.entity.SolicitudEntity;
    import org.springframework.data.r2dbc.repository.Query;
    import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
    import org.springframework.data.repository.reactive.ReactiveCrudRepository;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, Long>
            , ReactiveQueryByExampleExecutor<SolicitudEntity> {
        Flux<SolicitudEntity> findByIdEstado(Long idEstado);

        @Query("SELECT COUNT(*) FROM solicitud_hu4 WHERE documento_identidad = :documento AND id_estado = :idEstado")
        Mono<Integer> contarSolicitudesAprobadasPorDocumento(String documento, Long idEstado);
    }
