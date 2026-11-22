package br.com.balaio.events.event.application.usecase;

import br.com.balaio.events.event.application.port.EventoRepositoryPort;
import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.service.CalculadoraDistancia;

import java.util.Comparator;
import java.util.List;

public class ConsultarEventosPublicosUseCase {

    private final EventoRepositoryPort repository;

    public ConsultarEventosPublicosUseCase(EventoRepositoryPort repository){
        this.repository = repository;
    }

    public List<Evento> executar(){
        return repository.buscarEventosPublicos();
    }

    public List<Evento> executar(Double latitude, Double longitude) {

        if (latitude == null && longitude == null) {
            return repository.buscarEventosPublicos();
        }

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Latitude e longitude devem ser informadas juntas"
            );
        }

        validarCoordenadas(latitude, longitude);

        List<Evento> eventos = repository.buscarEventosPublicos();

        return eventos.stream()
                .sorted(Comparator.comparingDouble(evento ->
                        CalculadoraDistancia.calcular(
                                latitude,
                                longitude,
                                evento.latitude(),
                                evento.longitude()
                        )
                ))
                .toList();
    }

    private void validarCoordenadas(Double latitude, Double longitude) {

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {

            throw new IllegalArgumentException(
                    "Coordenadas geográficas inválidas"
            );
        }
    }
}