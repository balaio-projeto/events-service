package br.com.balaio.events.event.infrastructure.Config;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.application.usecase.ConsultarEventoPorSlugUseCase;
import br.com.balaio.events.event.application.usecase.ConsultarEventosPublicosUseCase;
import br.com.balaio.events.event.application.usecase.CriarEventoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventoConfig {

    @Bean
    public ConsultarEventosPublicosUseCase consultarEventosPublicosUseCase( EventoRepositoryPort repository) {
        return new ConsultarEventosPublicosUseCase(repository);
    }

    @Bean
    public ConsultarEventoPorSlugUseCase consultarEventoPorSlugUseCase(EventoRepositoryPort repository) {
        return new ConsultarEventoPorSlugUseCase(repository);
    }

    @Bean
    public CriarEventoUseCase criarEventoUseCase(EventoRepositoryPort repository) {
        return new CriarEventoUseCase(repository);
    }
}