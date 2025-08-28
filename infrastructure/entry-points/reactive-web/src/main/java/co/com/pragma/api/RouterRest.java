package co.com.pragma.api;

import co.com.pragma.api.dto.SolicitudRequest;
import co.com.pragma.api.dto.SolicitudResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    beanClass = Handler.class,
                    beanMethod = "crearSolicitud",
                    operation = @Operation(
                            operationId = "crearSolicitud",
                            summary = "Crear una nueva solicitud de crédito",
                            description = "Recibe los datos de una solicitud de crédito y los guarda en la base de datos",
                            tags = {"Solicitudes"},
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    description = "Datos de la solicitud de crédito",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SolicitudRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud creada correctamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = SolicitudResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/{idEstado}",
                    beanClass = Handler.class,
                    beanMethod = "listarSolicitudesPorEstado",
                    operation = @Operation(
                            operationId = "listarSolicitudesPorEstado",
                            summary = "Listar solicitudes por estado",
                            description = "Obtiene todas las solicitudes que tienen un estado específico",
                            tags = {"Solicitudes"},
                            parameters = {
                                    @Parameter(
                                            name = "idEstado",
                                            description = "ID del estado de las solicitudes",
                                            required = true,
                                            schema = @Schema(type = "integer", example = "1")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de solicitudes encontradas",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = SolicitudResponse.class))
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "204",
                                            description = "No se encontraron solicitudes con ese estado"
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "El idEstado no es válido"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/{idSolicitud}/estado/{idEstado}\"",
                    beanClass = Handler.class,
                    beanMethod = "cambiarEstadoSolicitud",
                    operation = @Operation(
                            operationId = "cambiarEstadoSolicitud",
                            summary = "Cambiar estado de una solicitud",
                            description = "Modifica el estado de una solicitud de crédito existente",
                            tags = {"Solicitudes"},
                            parameters = {
                                    @Parameter(
                                            name = "idSolicitud",
                                            description = "ID de la solicitud",
                                            required = true,
                                            schema = @Schema(type = "integer", example = "5")
                                    ),
                                    @Parameter(
                                            name = "idEstado",
                                            description = "Nuevo ID de estado",
                                            required = true,
                                            schema = @Schema(type = "integer", example = "2")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "204",
                                            description = "Estado actualizado correctamente"
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Parámetros inválidos"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "No se encontró la solicitud"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::crearSolicitud)
                .andRoute(GET("/api/v1/solicitud/{idEstado}"), handler::listarSolicitudesPorEstado)
                .andRoute(PUT("/api/v1/solicitud/{idSolicitud}/estado/{idEstado}"), handler::cambiarEstadoSolicitud);
    }
}

