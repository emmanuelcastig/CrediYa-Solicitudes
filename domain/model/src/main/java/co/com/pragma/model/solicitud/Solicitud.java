package co.com.pragma.model.solicitud;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private Long idSolicitud;
    private String nombre;
    private String apellido;
    private String documentoIdentidad;
    private BigDecimal monto;
    private int plazo;
    private BigDecimal tasaInteres;
    private BigDecimal salarioBase;
    private BigDecimal deudaMensual;
    private int solicitudesAprobadas;
    private String email;
    private Long idEstado;
    private Long idTipoPrestamo;

}
