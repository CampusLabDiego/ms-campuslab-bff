package cl.campuslab.bff.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) {

        JwtGrantedAuthoritiesConverter rolesConverter =
                new JwtGrantedAuthoritiesConverter();

        rolesConverter.setAuthoritiesClaimName("roles");
        rolesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter
                .setJwtGrantedAuthoritiesConverter(rolesConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)

                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()
                )

                .requestCache(cache -> cache.requestCache(
                        NoOpServerRequestCache.getInstance()
                ))

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource
                ))

                .authorizeExchange(auth -> auth

                        .pathMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        .pathMatchers(
                                "/api/admin",
                                "/api/admin/**"
                        )
                        .hasRole("Admin")

                        .pathMatchers(
                                "/api/report",
                                "/api/report/**"
                        )
                        .hasRole("Admin")

                        .pathMatchers(
                                "/api/audit",
                                "/api/audit/**"
                        )
                        .hasAnyRole("Admin", "Auditor")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/catalog",
                                "/api/catalog/**"
                        )
                        .hasAnyRole("Admin", "Operador", "Cliente")

                        .pathMatchers(
                                "/api/catalog",
                                "/api/catalog/**"
                        )
                        .hasRole("Admin")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/bookings",
                                "/api/bookings/**"
                        )
                        .hasAnyRole("Admin", "Operador", "Cliente")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/bookings"
                        )
                        .hasAnyRole("Admin", "Operador", "Cliente")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/bookings/*/arrival"
                        )
                        .hasRole("Cliente")

                        .pathMatchers(
                                "/api/bookings",
                                "/api/bookings/**"
                        )
                        .hasAnyRole("Admin", "Operador")

                        .anyExchange()
                        .denyAll()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(
                                        authenticationConverter
                                )
                        ))
                )

                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuer,

            @Value("${campuslab.security.audience}")
            String audience
    ) {

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withIssuerLocation(issuer)
                        .build();

        JwtClaimValidator<List<String>> audienceValidator =
                new JwtClaimValidator<>(
                        "aud",
                        audiences -> audiences != null
                                && audiences.contains(audience)
                );

        JwtClaimValidator<String> scopeValidator =
                new JwtClaimValidator<>(
                        "scp",
                        scopes -> scopes != null
                                && Arrays.asList(
                                        scopes.split("\\s+")
                                ).contains("access_as_user")
                );

        JwtClaimValidator<Object> expirationRequired =
                new JwtClaimValidator<>(
                        "exp",
                        value -> value != null
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<Jwt>(
                        JwtValidators.createDefaultWithIssuer(issuer),
                        audienceValidator,
                        scopeValidator,
                        expirationRequired
                )
        );

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${campuslab.security.allowed-origins:http://localhost:4200}")
            String allowedOrigins
    ) {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}