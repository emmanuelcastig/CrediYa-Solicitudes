package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.SolicitudRequest;
import co.com.pragma.api.dto.SolicitudResponse;
import co.com.pragma.model.solicitud.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudMapperTest {

    private SolicitudMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(SolicitudMapper.class);
    }

    @Test
    void toDomain_DeberiaMapearRequestCompletoADominio() {
        // Given
        SolicitudRequest request = new SolicitudRequest();
        request.setDocumentoIdentidad("123456789");
        request.setMonto(new BigDecimal("5000.75"));
        request.setPlazo(24);
        request.setEmail("test@example.com");
        request.setIdTipoPrestamo(1L);

        // When
        Solicitud domain = mapper.toDomain(request);

        // Then
        assertNotNull(domain);
        assertEquals("123456789", domain.getDocumentoIdentidad());
        assertEquals(new BigDecimal("5000.75"), domain.getMonto());
        assertEquals(24, domain.getPlazo());
        assertEquals("test@example.com", domain.getEmail());
        assertEquals(1L, domain.getIdTipoPrestamo());
    }

    @Test
    void toDomain_DeberiaManejarRequestConValoresNull() {
        // Given
        SolicitudRequest request = new SolicitudRequest();
        request.setDocumentoIdentidad("123456789");
        // monto es null
        request.setPlazo(12);
        request.setEmail("test@example.com");
        // idTipoPrestamo es null

        // When
        Solicitud domain = mapper.toDomain(request);

        // Then
        assertNotNull(domain);
        assertEquals("123456789", domain.getDocumentoIdentidad());
        assertNull(domain.getMonto());
        assertEquals(12, domain.getPlazo());
        assertEquals("test@example.com", domain.getEmail());
        assertNull(domain.getIdTipoPrestamo());
    }

    @Test
    void toDomain_DeberiaManejarRequestConValoresMinimos() {
        // Given
        SolicitudRequest request = new SolicitudRequest();
        request.setDocumentoIdentidad("1");
        request.setMonto(new BigDecimal("0.01"));
        request.setPlazo(1);
        request.setEmail("a@b.c");
        request.setIdTipoPrestamo(1L);

        // When
        Solicitud domain = mapper.toDomain(request);

        // Then
        assertNotNull(domain);
        assertEquals("1", domain.getDocumentoIdentidad());
        assertEquals(new BigDecimal("0.01"), domain.getMonto());
        assertEquals(1, domain.getPlazo());
        assertEquals("a@b.c", domain.getEmail());
        assertEquals(1L, domain.getIdTipoPrestamo());
    }


    @Test
    void toDomain_DeberiaRetornarNullCuandoRequestEsNull() {
        // When
        Solicitud domain = mapper.toDomain(null);

        // Then
        assertNull(domain);
    }

    @Test
    void toResponse_DeberiaManejarDominioSinId() {
        // Given
        Solicitud domain = new Solicitud();
        // idSolicitud es null
        domain.setDocumentoIdentidad("123456789");
        domain.setMonto(new BigDecimal("1000.00"));
        domain.setPlazo(12);
        domain.setEmail("test@example.com");

        // When
        SolicitudResponse response = mapper.toResponse(domain);

        // Then
        assertNotNull(response);
        assertNull(response.getIdSolicitud());
        assertEquals("123456789", response.getDocumentoIdentidad());
        assertEquals(new BigDecimal("1000.00"), response.getMonto());
        assertEquals(12, response.getPlazo());
        assertEquals("test@example.com", response.getEmail());
    }


}