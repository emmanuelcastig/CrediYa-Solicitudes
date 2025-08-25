package co.com.pragma.api.dto;

import co.com.pragma.model.enums.Estado;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitudResponse {
    private Long idSolicitud;
    private String documentoIdentidad;
    private BigDecimal monto;
    private int plazo;
    private String email;
    private Estado estado;
    private Long idTipoPrestamo;
}
