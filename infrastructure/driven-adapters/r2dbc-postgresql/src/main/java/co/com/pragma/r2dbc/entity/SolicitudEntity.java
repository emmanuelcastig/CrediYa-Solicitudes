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
@Table("solicitud_hu3")
public class SolicitudEntity {
    @Id
    private Long idSolicitud;
    private String documentoIdentidad;
    private BigDecimal monto;
    private int plazo;
    private String email;
    private String estado;
    private Long idTipoPrestamo;
}