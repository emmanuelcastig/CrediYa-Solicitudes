package co.com.pragma.consumer.aws;

import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidacionConsumer {

    private final SolicitudRepository solicitudRepository;

    public Mono<Void> procesarResultado(String mensaje) {
        log.info("Mensaje recibido en ValidacionConsumer: {}", mensaje);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(mensaje);

            Long idSolicitud = json.get("idSolicitud").asLong();
            String decision = json.get("decision").asText();

            log.info("Parsed mensaje -> idSolicitud: {}, decision: {}", idSolicitud, decision);

            Long idEstado = switch (decision) {
                case "APROBADO" -> 2L;
                case "RECHAZADO" -> 4L;
                case "REVISION MANUAL" -> 3L;
                default -> 1L;
            };

            log.info("Actualizando estado de la solicitud {} a estado {}", idSolicitud, idEstado);

            return solicitudRepository.actualizarEstadoSolicitud(idSolicitud, idEstado)
                    .doOnSuccess(v -> log.info("Estado actualizado correctamente para solicitud {}", idSolicitud))
                    .doOnError(err -> log.error("Error actualizando estado para solicitud {}", idSolicitud, err))
                    .then();

        } catch (Exception e) {
            log.error("Error procesando mensaje: {}", mensaje, e);
            return Mono.error(e);
        }
    }
}