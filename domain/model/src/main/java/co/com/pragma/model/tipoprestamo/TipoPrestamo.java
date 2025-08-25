package co.com.pragma.model.tipoprestamo;
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
public class TipoPrestamo {
    private Long idTipoPrestamo;
    private String nombre;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private Double tasaInteres;
    private Boolean validacionAutomatica;
}
