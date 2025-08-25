package co.com.pragma.consumer;

import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RestConsumer implements SolicitanteConsumerGateway {
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "verificarExistenciaSolicitante")
    public Mono<Boolean> verificarExistenciaSolicitante(String documentoIdentidad) {
        return client
                .get()
                .uri("/api/v1/usuarios/{documento}", documentoIdentidad)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Error servidor: " + response.statusCode())))
                .bodyToMono(ObjectResponse.class)
                .timeout(Duration.ofSeconds(5))
                .map(ObjectResponse::getExiste);
    }
}
