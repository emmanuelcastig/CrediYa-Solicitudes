package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.SolicitudRequest;
import co.com.pragma.api.dto.SolicitudResponse;
import co.com.pragma.model.solicitud.Solicitud;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {
    Solicitud toDomain(SolicitudRequest request);

    SolicitudResponse toResponse(Solicitud domain);
}