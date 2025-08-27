package co.com.pragma.r2dbc;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class TipoPrestamoReactiveRepositoryAdapterTest {

    @Mock
    private TipoPrestamoReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private TipoPrestamoReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new TipoPrestamoReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void testFindByIdTipoPrestamo() {

        Long id = 1L;
        TipoPrestamoEntity entity = new TipoPrestamoEntity();
        entity.setIdTipoPrestamo(id);
        entity.setNombre("Préstamo de Vivienda");

        TipoPrestamo domain = new TipoPrestamo();
        domain.setIdTipoPrestamo(id);
        domain.setNombre("Préstamo de Vivienda");

        when(repository.findByIdTipoPrestamo(id)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, TipoPrestamo.class)).thenReturn(domain);


        StepVerifier.create(adapter.findByIdTipoPrestamo(id))
                .expectNextMatches(tp -> tp.getIdTipoPrestamo().equals(id)
                        && tp.getNombre().equals("Préstamo de Vivienda"))
                .verifyComplete();
    }

    @Test
    void testFindByIdTipoPrestamoNotFound() {

        Long id = 99L;
        when(repository.findByIdTipoPrestamo(id)).thenReturn(Mono.empty());


        StepVerifier.create(adapter.findByIdTipoPrestamo(id))
                .verifyComplete();
    }
}
