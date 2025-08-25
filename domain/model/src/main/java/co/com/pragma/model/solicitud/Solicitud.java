package co.com.pragma.model.solicitud;

import co.com.pragma.model.enums.Estado;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private Long idSolicitud;
    private String documentoIdentidad;
    private BigDecimal monto;
    private int plazo;
    private String email;
    private Estado estado;
    private Long idTipoPrestamo;

}
