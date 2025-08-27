CREATE TABLE IF NOT EXISTS tipo_prestamo (
                                             id_tipo_prestamo BIGSERIAL PRIMARY KEY,
                                             nombre VARCHAR(100) NOT NULL,
    monto_minimo DECIMAL(15,2) NOT NULL,
    monto_maximo DECIMAL(15,2) NOT NULL,
    tasa_interes DECIMAL(5,2) NOT NULL,
    validacion_automatica BOOLEAN NOT NULL DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS solicitud_hu4 (
                                         id_solicitud BIGSERIAL PRIMARY KEY,
                                         nombre VARCHAR(150) NOT NULL,
    apellido VARCHAR(200) NOT NULL,
    documento_identidad VARCHAR(50) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    plazo INT NOT NULL,
    tasa_interes DECIMAL(5,2) NOT NULL,
    salario_base DECIMAL(15,2) NOT NULL,
    deuda_mensual DECIMAL(15,2),
    solicitudes_aprobadas INT DEFAULT 0,
    email VARCHAR(150) NOT NULL,
    id_estado BIGINT NOT NULL,
    id_tipo_prestamo BIGINT NOT NULL,
    CONSTRAINT fk_solicitud_tipo_prestamo
    FOREIGN KEY (id_tipo_prestamo) REFERENCES tipo_prestamo(id_tipo_prestamo),
    CONSTRAINT fk_solicitud_estado
    FOREIGN KEY (id_estado) REFERENCES estado(id_estado)
    );

CREATE TABLE IF NOT EXISTS estado (
                                             id_estado BIGSERIAL PRIMARY KEY,
                                             nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500) NOT NULL
    );