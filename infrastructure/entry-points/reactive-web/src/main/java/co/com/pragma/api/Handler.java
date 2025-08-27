package co.com.pragma.api;

import co.com.pragma.api.dto.SolicitudRequest;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.api.security.PermisoValidator;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final CrearSolicitudCredito crearSolicitudCredito;
    private final Validator validator;
    private final TransactionalOperator transactionalOperator;
    private final SolicitudMapper solicitudMapper;

    public Mono<ServerResponse> crearSolicitud(ServerRequest serverRequest) {
        log.trace("Iniciando creacion de solicitud desde request");

        String token = (String) serverRequest.attributes().get("token");
        String email = (String) serverRequest.attributes().get("email");
        String rol = (String) serverRequest.attributes().get("rol");


        log.debug("Token recibido: {}", token);
        log.debug("Email extraído del token: {}", email);

        return serverRequest.bodyToMono(SolicitudRequest.class)
                .doOnNext(request -> log.debug("Payload recibido: {}", request))
                .flatMap(this::validacion)
                .doOnNext(valid -> log.trace("Payload validado correctamente"))
                .flatMap(request ->
                        PermisoValidator.validarCreacionSolicitud(email, rol, request)
                                .doOnSuccess(valid -> log.trace("Validación de permisos " +
                                                "exitosa para usuario={} rol={} request={}",
                                        email, rol, request))
                                .doOnError(err -> log.error("Error en validación de permisos para " +
                                                "usuario={} rol={} documentoIdentidad={} -> {}", email, rol,
                                        err.getMessage()))
                )
                .map(solicitudMapper::toDomain)
                .doOnNext(domain -> log.debug("Objeto de dominio generado: {}", domain))
                .flatMap(solicitud -> crearSolicitudCredito.crearSolicitud(solicitud)
                        .as(transactionalOperator::transactional))
                .contextWrite(ctx -> ctx.put("token", token))
                .map(solicitudMapper::toResponse)
                .doOnSuccess(saved -> log.info("Solicitud creada exitosamente: {}", saved))
                .doOnError(error -> log.error("Error al crear solicitud", error))
                .flatMap(saved -> {
                    log.trace("Construyendo respuesta HTTP 201 para solicitud: {}", saved);
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(saved);
                });
    }

    public Mono<SolicitudRequest> validacion(SolicitudRequest request) {
        Set<ConstraintViolation<SolicitudRequest>> violaciones = validator.validate(request);
        if (!violaciones.isEmpty()) {
            String errorMessage = violaciones.stream()
                    .map(violation -> violation.getPropertyPath() + ": " +
                            violation.getMessage())
                    .collect(Collectors.joining(", "));
            return Mono.error(new ValidationException(errorMessage));
        }
        return Mono.just(request);
    }
}
