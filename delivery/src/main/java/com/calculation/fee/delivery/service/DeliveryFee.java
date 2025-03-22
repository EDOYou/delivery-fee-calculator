package com.calculation.fee.delivery.service;

import com.calculation.fee.delivery.exception.UsageForbiddenException;
import com.calculation.fee.delivery.model.City;
import com.calculation.fee.delivery.model.VehicleType;
import com.calculation.fee.delivery.model.Weather;
import com.calculation.fee.delivery.repository.WeatherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class DeliveryFee {

    private final WeatherRepository weatherRepository;

    public DeliveryFee(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    /**
     * Calculates the total delivery fee for a food courier based on the city and vehicle type.
     * <p>
     * This method retrieves the latest weather data for the specified city and calculates the delivery fee
     * by combining the regional base fee (RBF) with extra fees based on weather conditions:
     * - Air Temperature Extra Fee (ATEF): Applied for Scooter or Bike if temperature is below 0°C.
     * - Wind Speed Extra Fee (WSEF): Applied for Bike if wind speed is between 10 m/s and 20 m/s.
     * - Weather Phenomenon Extra Fee (WPEF): Applied for Scooter or Bike for snow, sleet, or rain.
     * </p>
     * <p>
     * The method throws exceptions for invalid conditions:
     * <p>- If no weather data is available for the city, an IllegalStateException is thrown.
     * <p>- If weather conditions forbid the usage of the vehicle type (e.g., wind speed > 20 m/s for Bike,
     *   or glaze/hail/thunder for Scooter/Bike), a UsageForbiddenException is thrown.
     * </p>
     *
     * @param city        The city for delivery (e.g., Tallinn, Tartu, Pärnu). Must not be null.
     * @param vehicleType The vehicle type (e.g., Car, Scooter, Bike). Must not be null.
     * @return The total delivery fee, including the regional base fee and any applicable extra fees.
     * @throws IllegalStateException If no weather data is available for the specified city.
     * @throws UsageForbiddenException If weather conditions forbid the usage of the vehicle type.
     */
    public double calculateDeliveryFee(City city, VehicleType vehicleType) {
        Optional<Weather> latestWeather = weatherRepository.getLatestWeatherForStation(city.getStationName());
        if (latestWeather.isEmpty()) {
            log.warn("No weather data available for station: {}", city.getStationName());
            throw new IllegalStateException("No weather data available for " + city.getStationName());
        }

        Weather weather = latestWeather.get();
        log.info("Calculating fee for city: {}, vehicle: {}, weather: {}", city, vehicleType, weather);

        double baseFee = calculateRegionalBaseFee(city, vehicleType);
        double atef = calculateAirTemperatureExtraFee(weather.getAirTemperature());
        double wsef = calculateWindSpeedExtraFee(weather.getWindSpeed(), vehicleType);
        double wpef = calculateWeatherPhenomenonExtraFee(weather.getWeatherPhenomenon(), vehicleType);

        double totalFee = baseFee + atef + wsef + wpef;
        log.info("Total delivery fee: {} (RBF: {}, ATEF: {}, WSEF: {}, WPEF: {})", totalFee, baseFee, atef, wsef, wpef);
        return totalFee;
    }

    private double calculateRegionalBaseFee(City city, VehicleType vehicleType) {
        switch (city) {
            case TALLINN:
                return switch (vehicleType) {
                    case CAR -> 4.0;
                    case SCOOTER -> 3.5;
                    case BIKE -> 3.0;
                };
            case TARTU:
                return switch (vehicleType) {
                    case CAR -> 3.5;
                    case SCOOTER -> 3.0;
                    case BIKE -> 2.5;
                };
            case PARNU:
                return switch (vehicleType) {
                    case CAR -> 3.0;
                    case SCOOTER -> 2.5;
                    case BIKE -> 2.0;
                };
        }
        throw new IllegalStateException("Invalid city or vehicle type combination");
    }

    private double calculateAirTemperatureExtraFee(Double airTemperature) {
        if (airTemperature == null) return 0.0;
        if (airTemperature < -10) return 1.0;
        if (airTemperature < 0) return 0.5;
        return 0.0;
    }

    private double calculateWindSpeedExtraFee(Double windSpeed, VehicleType vehicleType) {
        if (windSpeed == null) {
            return 0.0;
        }
        if (windSpeed >= 20 && (vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BIKE)) {
            throw new UsageForbiddenException("Usage of selected vehicle type is forbidden. Vehicle type: " + vehicleType.name() + " Wind speed: " + windSpeed + " m/s");
        }
        if (windSpeed >= 10 && windSpeed < 20) {
            return 0.5;
        }
        return 0.0;
    }

    private double calculateWeatherPhenomenonExtraFee(String phenomenon, VehicleType vehicleType) {
        if (phenomenon == null || phenomenon.trim().isEmpty()) {
            return 0.0;
        }
        String phenomenonLower = phenomenon.toLowerCase();

        if (vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BIKE) {
            if (phenomenonLower.contains("glaze") || phenomenonLower.contains("hail") || phenomenonLower.contains("thunder")) {
                throw new UsageForbiddenException("Usage of selected vehicle type is forbidden. Vehicle type: " + vehicleType.name() + ". Phenomenon: " + phenomenon);
            }
        }

        if (phenomenonLower.contains("snow") || phenomenonLower.contains("sleet")) {
            return 1.0;
        }
        if (phenomenonLower.contains("rain")) {
            return 0.5;
        }
        return 0.0;
    }
}