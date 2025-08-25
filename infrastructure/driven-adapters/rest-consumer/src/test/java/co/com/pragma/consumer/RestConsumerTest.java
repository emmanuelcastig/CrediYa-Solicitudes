package co.com.pragma.consumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class RestConsumerTest {

    private static RestConsumer restConsumer;
    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder()
                .baseUrl(mockBackEnd.url("/").toString())
                .build();
        restConsumer = new RestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void verificarExistenciaSolicitante_existeTrue() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"existe\": true}"));

        StepVerifier.create(restConsumer.verificarExistenciaSolicitante("123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void verificarExistenciaSolicitante_existeFalse() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"existe\": false}"));

        StepVerifier.create(restConsumer.verificarExistenciaSolicitante("999"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void verificarExistenciaSolicitante_serverError() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        StepVerifier.create(restConsumer.verificarExistenciaSolicitante("123"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().contains("Error servidor"))
                .verify();
    }

    @Test
    void verificarExistenciaSolicitante_timeout() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"existe\": true}")
                .setBodyDelay(6, java.util.concurrent.TimeUnit.SECONDS)
                .setResponseCode(HttpStatus.OK.value()));

        StepVerifier.create(restConsumer.verificarExistenciaSolicitante("123"))
                .expectErrorMatches(throwable -> throwable instanceof java.util.concurrent.TimeoutException)
                .verify();
    }
}
