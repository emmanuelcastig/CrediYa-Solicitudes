package co.com.pragma.config;

import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.SolicitudUseCase;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
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

    public UseCasesConfig(SolicitudRepository solicitudRepository, TipoPrestamoRepository tipoPrestamoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.tipoPrestamoRepository = tipoPrestamoRepository;
    }

    @Bean
    @Primary
    public CrearSolicitudCredito crearSolicitudCredito() {
        return new SolicitudUseCase(tipoPrestamoRepository, solicitudRepository);
    }
}
