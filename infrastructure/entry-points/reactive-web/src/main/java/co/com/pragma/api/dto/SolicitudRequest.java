package co.com.pragma.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitudRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El documento de identidad es obligatorio")
    private String documentoIdentidad;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    @Max(value = 360, message = "El plazo máximo es 360 meses")
    private int plazo;

    @NotNull(message = "La tasa de interes es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa de interes debe ser mayor a 0")
    private BigDecimal tasaInteres;

    @NotNull(message = "El salario base es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal salarioBase;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato valido")
    private String email;

    @NotNull(message = "El tipo de préstamo es obligatorio")
    private Long idTipoPrestamo;
}
