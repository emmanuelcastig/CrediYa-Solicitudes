package co.com.pragma.consumer.aws;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidacionListener {

    private final ValidacionConsumer validacionConsumer;

    @SqsListener("${spring.sqs.cola-resultados-validacion}")
    public void escucharCola(String mensaje) {
        log.info("Respuesta recibido desde SQS: {}", mensaje);

        validacionConsumer.procesarResultado(mensaje)
                .doOnSubscribe(sub -> log.info(" Iniciando procesamiento de respuesta lambda"))
                .doOnSuccess(res -> log.info("Procesamiento finalizado correctamente"))
                .doOnError(error -> log.error("Error durante procesamiento: {}", error.getMessage(), error))
                .subscribe();
    }
}