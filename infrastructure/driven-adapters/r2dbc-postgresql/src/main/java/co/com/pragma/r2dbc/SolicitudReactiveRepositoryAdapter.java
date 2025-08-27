package co.com.pragma.r2dbc;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
    Long,
        SolicitudReactiveRepository
> implements SolicitudRepository {
    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
    }


    @Override
    public Mono<Solicitud> guardarSolicitud(Solicitud solicitud) {
        return repository.save(toData(solicitud))
                .map(this::toEntity);
    }

    @Override
    public Flux<Solicitud> obtenerSolicitudesPorEstado(Long idEstado) {
        return repository.findByIdEstado(idEstado)
                .map(this::toEntity);
    }

    @Override
    public Mono<Integer> contarSolicitudesAprobadasPorDocumento(String documentoIdentidad, Long idEstado) {
        return repository.contarSolicitudesAprobadasPorDocumento(documentoIdentidad, idEstado);
    }
}
