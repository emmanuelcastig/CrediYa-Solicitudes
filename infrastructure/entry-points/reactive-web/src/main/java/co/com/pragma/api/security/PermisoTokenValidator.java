package co.com.pragma.api.security;

import co.com.pragma.api.dto.SolicitudRequest;
import reactor.core.publisher.Mono;

public class PermisoTokenValidator {

    private PermisoTokenValidator() {
    }

    public static Mono<SolicitudRequest> validarCreacionSolicitud(
            String email, String rol , SolicitudRequest request) {

        if (!("CLIENTE".equalsIgnoreCase(rol) || "ADMINISTRADOR".equalsIgnoreCase(rol))) {
            return Mono.error(new RuntimeException("Solo un CLIENTE puede crear solicitudes de préstamo"));
        }

        if (!request.getEmail().equals(email)) {
            return Mono.error(new RuntimeException("Un cliente solo puede crear solicitudes para si mismo"));
        }

        return Mono.just(request);
    }

    public static Mono<Void> validarAccesoListarSolicitudes(String rol) {
        if (!("ADMINISTRADOR".equalsIgnoreCase(rol) || "ASESOR".equalsIgnoreCase(rol))) {
            return Mono.error(new RuntimeException("Solo un ASESOR puede listar solicitudes"));
        }
        return Mono.empty();
    }

    public static Mono<Void> validarAccesoEditarEstado(String rol) {
        if (!("ADMINISTRADOR".equalsIgnoreCase(rol) || "ASESOR".equalsIgnoreCase(rol))) {
            return Mono.error(new RuntimeException("Solo un ASESOR puede editar estados de solicitudes"));
        }
        return Mono.empty();
    }
}