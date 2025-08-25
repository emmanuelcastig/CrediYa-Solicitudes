package co.com.pragma.r2dbc;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TipoPrestamoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        TipoPrestamo,
        TipoPrestamoEntity,
        Long,
        TipoPrestamoReactiveRepository
        > implements TipoPrestamoRepository {
    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, TipoPrestamo.class));
    }


    @Override
    public Mono<TipoPrestamo> findByIdTipoPrestamo(Long idTipoPrestamo) {
        return repository.findByIdTipoPrestamo(idTipoPrestamo)
                .map(this::toEntity);
    }
}
