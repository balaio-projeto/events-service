package br.com.balaio.events.security;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Autorização local do events-service (PI-210). O gateway é uma camada extra; esta configuração
 * valida o Bearer com a chave pública e decide o acesso sem depender da borda.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(EventosJwtProperties.class)
class SegurancaEventosConfig {

    @Bean
    SecurityFilterChain cadeiaSegurancaEventos(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint naoAutenticado = (request, response, exception) ->
                escreverErro(response, HttpServletResponse.SC_UNAUTHORIZED, "NAO_AUTENTICADO", "Autenticação necessária.");
        AccessDeniedHandler semPermissao = (request, response, exception) ->
                escreverErro(response, HttpServletResponse.SC_FORBIDDEN, "SEM_PERMISSAO", "Operação não permitida.");

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos", "/api/v1/eventos/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/eventos").hasAuthority("PAPEL_ORGANIZADOR")
                        .requestMatchers("/api/v1/admin/**").hasAuthority("PAPEL_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(semPermissao))
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePapel()))
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(semPermissao))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(EventosJwtProperties properties) {
        RSAPublicKey chave = carregarChavePublica(arquivoDaChave(properties.publicKeyLocation()));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(chave)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                        audiencia -> audiencia != null && audiencia.contains(properties.audience())),
                new ValidadorClaimsEvento(Clock.systemUTC())));
        return decoder;
    }

    /** Converte o claim papel em uma autoridade PAPEL_&lt;PAPEL&gt;; nenhum header ou corpo participa. */
    static JwtAuthenticationConverter conversorDePapel() {
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(SegurancaEventosConfig::autoridadesDoPapel);
        return conversor;
    }

    private static Collection<GrantedAuthority> autoridadesDoPapel(Jwt jwt) {
        String papel = jwt.getClaimAsString("papel");
        return List.of(new SimpleGrantedAuthority("PAPEL_" + papel.toUpperCase(Locale.ROOT)));
    }

    /** Aceita caminho simples ou URI file:, o mesmo formato usado pelo gateway e pelo profile no compose. */
    static Path arquivoDaChave(String local) {
        return local.startsWith("file:") ? Path.of(URI.create(local)) : Path.of(local);
    }

    static RSAPublicKey carregarChavePublica(Path arquivo) {
        try {
            if (!Files.isRegularFile(arquivo) || Files.size(arquivo) > 16384) {
                throw new IllegalArgumentException();
            }
            String pem = Files.readString(arquivo, StandardCharsets.US_ASCII).strip();
            String inicio = "-----BEGIN PUBLIC KEY-----";
            String fim = "-----END PUBLIC KEY-----";
            if (!pem.startsWith(inicio) || !pem.endsWith(fim)) {
                throw new IllegalArgumentException();
            }
            byte[] der = Base64.getMimeDecoder().decode(pem.substring(inicio.length(), pem.length() - fim.length()));
            var chave = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
            if (!(chave instanceof RSAPublicKey rsa) || rsa.getModulus().bitLength() < 3072) {
                throw new IllegalArgumentException();
            }
            return rsa;
        } catch (Exception exception) {
            throw new IllegalStateException("Configuração da chave pública JWT inválida.");
        }
    }

    private static void escreverErro(HttpServletResponse response, int status, String codigo, String mensagem)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("{\"dados\":null,\"erro\":{\"codigo\":\"" + codigo
                + "\",\"mensagem\":\"" + mensagem + "\"}}");
    }
}
