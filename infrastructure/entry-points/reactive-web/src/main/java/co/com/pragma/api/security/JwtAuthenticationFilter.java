package co.com.pragma.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new RuntimeException("Token no enviado"));
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validateToken(token)) {
            return Mono.error(new RuntimeException("Token inválido"));
        }

        String email = jwtProvider.extractEmail(token);
        String rol = jwtProvider.extractRol(token);

        // Guardar en atributos del request
        exchange.getAttributes().put("token", token);
        exchange.getAttributes().put("email", email);
        exchange.getAttributes().put("rol", rol);
        return chain.filter(exchange);
    }
}