    package co.com.pragma.r2dbc;

    import co.com.pragma.r2dbc.entity.SolicitudEntity;
    import org.springframework.data.r2dbc.repository.Modifying;
    import org.springframework.data.r2dbc.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
    import org.springframework.data.repository.reactive.ReactiveCrudRepository;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    import java.math.BigDecimal;

    public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, Long>
            , ReactiveQueryByExampleExecutor<SolicitudEntity> {
        Flux<SolicitudEntity> findByIdEstado(Long idEstado);

        Mono<SolicitudEntity> findByIdSolicitud(Long idSolicitud);

        @Query("SELECT COUNT(*) FROM solicitud_hu4 WHERE documento_identidad = :documento AND id_estado = :idEstado")
        Mono<Integer> contarSolicitudesAprobadasPorDocumento(String documento, Long idEstado);

        @Modifying
        @Query("UPDATE solicitud_hu4 SET id_estado = :idEstado WHERE id_solicitud = :idSolicitud")
        Mono<Integer> actualizarEstadoSolicitud(@Param("idSolicitud") Long idSolicitud,
                                                @Param("idEstado") Long idEstado);

        @Query("SELECT COALESCE(SUM(deuda_mensual), 0) " +
                "FROM solicitud_hu4 " +
                "WHERE documento_identidad = :documentoIdentidad " +
                "AND id_estado = :idEstado")
        Mono<BigDecimal> sumarCuotasMensualesEnSolicitudesAprobadas(@Param("documentoIdentidad") String documentoIdentidad,
                                                                    @Param("idEstado") Long idEstado);
    }
