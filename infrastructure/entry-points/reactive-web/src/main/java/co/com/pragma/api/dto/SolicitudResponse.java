package co.com.pragma.api.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitudResponse {
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
    private String estado;
    private String tipoPrestamo;
}
