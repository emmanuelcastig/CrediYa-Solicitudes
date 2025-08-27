package co.com.pragma.config;

import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.SolicitudUseCase;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ComponentScan(basePackages = "co.com.pragma.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {
    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final SolicitanteConsumerGateway solicitanteConsumerGateway;

    public UseCasesConfig(SolicitudRepository solicitudRepository, TipoPrestamoRepository tipoPrestamoRepository, SolicitanteConsumerGateway solicitanteConsumerGateway) {
        this.solicitudRepository = solicitudRepository;
        this.tipoPrestamoRepository = tipoPrestamoRepository;
        this.solicitanteConsumerGateway = solicitanteConsumerGateway;
    }

    @Bean
    @Primary
    public CrearSolicitudCredito crearSolicitudCredito() {
        return new SolicitudUseCase(tipoPrestamoRepository, solicitudRepository,solicitanteConsumerGateway);
    }

   /* @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9001")
                .filter((request , response) -> {
                    return response.exchange(ClientRequest.from(request).header("Authorization","Bearer ").build());
                }))// Cambia esto por la URL real del servicio externo
                .build();
    }

    public String obtenerToken(){

    }*/
}
