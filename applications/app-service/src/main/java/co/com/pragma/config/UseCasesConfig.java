package co.com.pragma.config;

import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.SolicitudUseCase;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import co.com.pragma.model.consumer.SolicitanteConsumerGateway;
import org.springframework.context.annotation.*;

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
}
