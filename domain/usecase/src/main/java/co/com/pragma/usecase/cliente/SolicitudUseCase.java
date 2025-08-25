package co.com.pragma.usecase.cliente;

import co.com.pragma.model.enums.Estado;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.usecase.cliente.in.CrearSolicitudCredito;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase implements CrearSolicitudCredito {
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final SolicitudRepository solicitudRepository;


    @Override
    public Mono<Solicitud> crearSolicitud(Solicitud solicitud) {
        solicitud.setEstado(Estado.PENDIENTE);
        Mono<TipoPrestamo> tipoPrestamoMono = tipoPrestamoRepository.findByIdTipoPrestamo(
                        solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de préstamo no existe")));

        return tipoPrestamoMono.flatMap(tipoPrestamo ->
                solicitudRepository.guardarSolicitud(solicitud));
    }
}
