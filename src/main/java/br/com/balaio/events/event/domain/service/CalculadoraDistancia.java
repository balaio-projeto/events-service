package br.com.balaio.events.event.domain.service;

public final class CalculadoraDistancia {

    private static final double RAIO_TERRA_KM = 6371.0;

    private CalculadoraDistancia() {
    }

    public static double calcular(
            double latitudeOrigem,
            double longitudeOrigem,
            double latitudeDestino,
            double longitudeDestino
    ) {
        double deltaLatitude = Math.toRadians(
                latitudeDestino - latitudeOrigem
        );

        double deltaLongitude = Math.toRadians(
                longitudeDestino - longitudeOrigem
        );

        double a = Math.pow(Math.sin(deltaLatitude / 2), 2)
                + Math.cos(Math.toRadians(latitudeOrigem))
                * Math.cos(Math.toRadians(latitudeDestino))
                * Math.pow(Math.sin(deltaLongitude / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(Math.max(0.0, 1.0 - a))
        );

        return RAIO_TERRA_KM * c;
    }
}