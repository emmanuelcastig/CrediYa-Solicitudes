package co.com.pragma.api.security;

import reactor.core.publisher.Mono;

public class PermisoListaValidator {

    public static Mono<Void> validarAccesoListarSolicitudes(String rol) {
        if (!("ADMINISTRADOR".equalsIgnoreCase(rol) || "ASESOR".equalsIgnoreCase(rol))) {
            return Mono.error(new RuntimeException("Solo un ASESOR puede listar solicitudes"));
        }
        return Mono.empty();
    }
}
