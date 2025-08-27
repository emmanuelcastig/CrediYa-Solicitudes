package co.com.pragma.api.security;

import co.com.pragma.api.dto.SolicitudRequest;
import reactor.core.publisher.Mono;

public class PermisoValidator {

    private PermisoValidator() {
    }

    public static Mono<SolicitudRequest> validarCreacionSolicitud(
            String email, String rol , SolicitudRequest request) {

        if (!"CLIENTE".equalsIgnoreCase(rol)) {
            return Mono.error(new RuntimeException("Solo un CLIENTE puede crear solicitudes de prestamo"));
        }

        if (!request.getEmail().equals(email)) {
            return Mono.error(new RuntimeException("Un cliente solo puede crear solicitudes para si mismo"));
        }

        return Mono.just(request);
    }
}