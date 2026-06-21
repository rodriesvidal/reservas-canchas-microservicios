package com.canchas.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    // Rutas que NO requieren token (registro y login)
    private static final List<String> RUTAS_PUBLICAS = List.of("/auth/");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Las rutas públicas pasan sin token
        if (esRutaPublica(path)) {
            return chain.filter(exchange);
        }

        // 2. Debe venir el header Authorization con "Bearer "
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rechazar(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. El token debe ser válido (firma + no expirado)
        Claims claims;
        try {
            claims = obtenerClaims(token);
        } catch (Exception e) {
            return rechazar(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 4. Revisar el rol contra la ruta solicitada
        String rol = claims.get("rol", String.class);
        if (!tienePermiso(request.getMethod(), path, rol)) {
            return rechazar(exchange, HttpStatus.FORBIDDEN);
        }

        // 5. Agregar la info del usuario en headers (por si un servicio la quiere usar) y dejar pasar
        ServerHttpRequest requestConUsuario = request.mutate()
                .header("X-User-Email", claims.getSubject())
                .header("X-User-Rol", rol)
                .build();
        return chain.filter(exchange.mutate().request(requestConUsuario).build());
    }

    private boolean esRutaPublica(String path) {
        return RUTAS_PUBLICAS.stream().anyMatch(path::startsWith);
    }

    private boolean tienePermiso(HttpMethod metodo, String path, String rol) {
        // El ADMIN puede hacer todo
        if ("ADMIN".equals(rol)) {
            return true;
        }
        // Crear, editar o eliminar canchas: solo PROPIETARIO
        if (path.startsWith("/canchas") && esEscritura(metodo)) {
            return "PROPIETARIO".equals(rol);
        }
        // Crear, editar o eliminar horarios: solo PROPIETARIO
        if (path.startsWith("/horarios") && esEscritura(metodo)) {
            return "PROPIETARIO".equals(rol);
        }
        // Crear una reserva: solo CLIENTE
        if (path.startsWith("/reservas") && metodo == HttpMethod.POST) {
            return "CLIENTE".equals(rol);
        }
        // Eliminar usuarios: solo ADMIN (los demás roles bloqueados)
        if (path.startsWith("/usuarios") && metodo == HttpMethod.DELETE) {
            return false;
        }
        // Todo lo demás (lecturas GET, etc.): cualquier usuario autenticado
        return true;
    }

    private boolean esEscritura(HttpMethod metodo) {
        return metodo == HttpMethod.POST
                || metodo == HttpMethod.PUT
                || metodo == HttpMethod.DELETE;
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, HttpStatus estado) {
        exchange.getResponse().setStatusCode(estado);
        return exchange.getResponse().setComplete();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public int getOrder() {
        return -1; // que se ejecute primero, antes del enrutamiento
    }
}