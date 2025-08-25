CREATE TABLE IF NOT EXISTS tipo_prestamo (
                                             id_tipo_prestamo BIGSERIAL PRIMARY KEY,
                                             nombre VARCHAR(100) NOT NULL,
    monto_minimo DECIMAL(15,2) NOT NULL,
    monto_maximo DECIMAL(15,2) NOT NULL,
    tasa_interes DECIMAL(5,2) NOT NULL,
    validacion_automatica BOOLEAN NOT NULL DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS solicitud (
                           id_solicitud BIGSERIAL PRIMARY KEY,
                           documento_identidad VARCHAR(50) NOT NULL,
                           monto DECIMAL(15,2) NOT NULL,
                           plazo INT NOT NULL,
                           email VARCHAR(150) NOT NULL,
                           estado VARCHAR(50) NOT NULL,
                           id_tipo_prestamo BIGINT NOT NULL,
                           CONSTRAINT fk_solicitud_tipo_prestamo
                               FOREIGN KEY (id_tipo_prestamo) REFERENCES tipo_prestamo(id_tipo_prestamo)
);