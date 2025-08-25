package co.com.pragma.model.enums;

public enum Estado {
    PENDIENTE("Pendiente","La solicitud está en proceso de revisión"),
    APROBADO("Aprobado","La solicitud ha sido aprobada"),
    RECHAZADO("Rechazado","La solicitud ha sido rechazada");

    private final String nombre;
    private final String descripcion;

    Estado(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
