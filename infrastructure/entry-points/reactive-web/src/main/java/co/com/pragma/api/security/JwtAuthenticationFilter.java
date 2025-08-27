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

        // 👉 Guardar claims para usarlos después en el UseCase
        exchange.getAttributes().put("username", jwtProvider.extractUsername(token));
        exchange.getAttributes().put("rol", jwtProvider.extractRol(token));

        return chain.filter(exchange);
    }
}
