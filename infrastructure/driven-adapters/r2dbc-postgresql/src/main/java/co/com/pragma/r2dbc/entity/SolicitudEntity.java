package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("solicitud_hu4")
public class SolicitudEntity {
    @Id
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