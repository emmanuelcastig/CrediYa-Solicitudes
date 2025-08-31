package co.com.pragma.consumer.aws;

import co.com.pragma.model.solicitud.gateways.SQSPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSService implements SQSPublisher {

    private final SqsClient sqsClient;

    @Value("${spring.sqs.cola-crediYa-url}")
    private String notificacionQueueUrl;

    @Value("${spring.sqs.cola-validacion-automatica-crediYa}")
    private String validacionQueueUrl;

    @Override
    public Mono<Void> enviarMensajeNotificacion(String mensaje) {
        return enviarMensaje(mensaje, notificacionQueueUrl);
    }

    @Override
    public Mono<Void> enviarMensajeValidacionAutomatica(String mensaje) {
        return enviarMensaje(mensaje, validacionQueueUrl);
    }

    private Mono<Void> enviarMensaje(String mensaje, String queueUrl) {
        return Mono.fromCallable(() -> {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(mensaje)
                    .build();

            var response = sqsClient.sendMessage(sendMsgRequest);

            log.info("Mensaje enviado a SQS [{}] con ID: {}", queueUrl, response.messageId());
            log.debug("Contenido del mensaje: {}", mensaje);

            return response;
        }).then();
    }
}