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

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;


    @Override
    public Mono<Void> enviarMensaje(String mensaje) {
        return Mono.fromCallable(() -> {

                    SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(mensaje)
                            .build();

                    var response = sqsClient.sendMessage(sendMsgRequest);

                    log.trace("Mensaje enviado a SQS con ID: {}", response.messageId());
                    log.trace("Contenido del mensaje: {}", mensaje);

                    return response;
                })
                .then();
    }
}
