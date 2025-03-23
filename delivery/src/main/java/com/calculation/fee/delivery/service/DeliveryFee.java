package com.calculation.fee.delivery.service;

import com.calculation.fee.delivery.exception.UsageForbiddenException;
import com.calculation.fee.delivery.model.BusinessRule;
import com.calculation.fee.delivery.model.City;
import com.calculation.fee.delivery.model.VehicleType;
import com.calculation.fee.delivery.model.Weather;
import com.calculation.fee.delivery.repository.BusinessRuleRepository;
import com.calculation.fee.delivery.repository.WeatherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * This service class responible for calculating delivery fees based on city, vehicle type, weather conditions,and latest business rule
 */
@Service
@Slf4j
public class DeliveryFee {

    private final WeatherRepository weatherRepository;
    private final BusinessRuleRepository businessRuleRepository;

    public DeliveryFee(WeatherRepository weatherRepository, BusinessRuleRepository businessRuleRepository) {
        this.weatherRepository = weatherRepository;
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Calculates the total delivery fee for a given city and vehicle type, considering weather conditions
     * and business rules for latest timestapm
     * <p>
     * The total fee is calculated as the sum of:
     * - Regional Base Fee (RBF): Base fee for the city and vehicle type
     * - Air Temperature Extra Fee (ATEF): Additional fee based on air temperature
     * - Wind Speed Extra Fee (WSEF): Additional fee based on wind speed, with restrictions for certain vehicle types
     * - Weather Phenomenon Extra Fee (WPEF): Additional fee based on weather phenomena, with restrictions for certain vehicle types
     * </p>
     *
     * @param city The cities are TALLINN, TARTU or PARNU
     * @param vehicleType The type of vehicle used are CAR, SCOOTER or BIKE
     * @param datetime
     * @return The total delivery fee
     * @throws IllegalStateException If no weather data or business rules are available for the specified city or datetime
     * @throws UsageForbiddenException If the weather conditions does not allow the use of the selected vehicle type
     * @see City
     * @see VehicleType
     * @see BusinessRule
     * @see Weather
     *
     * <p><b>Example Usage:</b></p>
     * <pre>
     * DeliveryFee deliveryFee = new DeliveryFee(weatherRepository, businessRuleRepository);
     * <p>
     * City city = City.TALLINN;
     * <p>
     * VehicleType vehicleType = VehicleType.CAR;
     * <p>
     * LocalDateTime datetime = LocalDateTime.of(2025, 3, 22, 10, 0);
     * <p>
     * double fee = feeService.calculateDeliveryFee(city, vehicleType, datetime);
     * <p>
     * System.out.println("Delivery fee: " + fee + " EUR");
     * </pre>
     */
    public double calculateDeliveryFee(City city, VehicleType vehicleType, LocalDateTime datetime) {
        Weather weather;
        BusinessRule businessRule;
        if (datetime == null) {
            weather = weatherRepository.getLatestWeatherForStation(city.getStationName())
                    .orElseThrow(() -> new IllegalStateException("No weather data available for " + city.getStationName()));
            businessRule = businessRuleRepository.findFirstByOrderByTimestampDesc()
                    .orElseThrow(() -> new IllegalStateException("No business rules available"));
        } else {
            weather = weatherRepository.getWeatherForStationAtOrBefore(city.getStationName(), datetime)
                    .orElseThrow(() -> new IllegalStateException("No business rules available"));
            businessRule = businessRuleRepository.findFirstByTimestampLessThanEqualOrderByTimestampDesc(datetime)
                    .orElseThrow(() -> new IllegalStateException("No business rules available at or before " + datetime));
        }

        log.info("Calculating fee for city: {}, vehicle: {}, weather: {}, datetime: {}. Using business rule: {}", city, vehicleType, weather, datetime, businessRule);

        double baseFee = calculateRegionalBaseFee(city, vehicleType, businessRule);
        double atef = calculateAirTemperatureExtraFee(weather.getAirTemperature(), businessRule);
        double wsef = calculateWindSpeedExtraFee(weather.getWindSpeed(), vehicleType, businessRule);
        double wpef = calculateWeatherPhenomenonExtraFee(weather.getWeatherPhenomenon(), vehicleType, businessRule);

        double totalFee = baseFee + atef + wsef + wpef;
        log.info("Total delivery fee: {} (RBF: {}, ATEF: {}, WSEF: {}, WPEF: {})", totalFee, baseFee, atef, wsef, wpef);
        return totalFee;
    }

    private double calculateRegionalBaseFee(City city, VehicleType vehicleType, BusinessRule businessRules) {
        switch (city) {
            case TALLINN:
                return switch (vehicleType) {
                    case CAR -> businessRules.getTallinnCarBaseFee();
                    case SCOOTER -> businessRules.getTallinnScooterBaseFee();
                    case BIKE -> businessRules.getTallinnBikeBaseFee();
                };
            case TARTU:
                return switch (vehicleType) {
                    case CAR -> businessRules.getTartuCarBaseFee();
                    case SCOOTER -> businessRules.getTartuScooterBaseFee();
                    case BIKE -> businessRules.getTartuBikeBaseFee();
                };
            case PARNU:
                return switch (vehicleType) {
                    case CAR -> businessRules.getParnuCarBaseFee();
                    case SCOOTER -> businessRules.getParnuScooterBaseFee();
                    case BIKE -> businessRules.getParnuBikeBaseFee();
                };
        }
        throw new IllegalStateException("Invalid city or vehicle type combination");
    }

    private double calculateAirTemperatureExtraFee(Double airTemperature, BusinessRule businessRules) {
        if (airTemperature == null) return 0.0;
        if (airTemperature < -10) return businessRules.getAtefBelowMinusTen();
        if (airTemperature < 0) return businessRules.getAtefBelowZero();
        return 0.0;
    }

    private double calculateWindSpeedExtraFee(Double windSpeed, VehicleType vehicleType, BusinessRule businessRules) {
        if (windSpeed == null) {
            return 0.0;
        }
        if (windSpeed >= 20 && (vehicleType == VehicleType.SCOOTER || vehicleType == VehicleType.BIKE)) {
            throw new UsageForbiddenException("Usage of selected vehicle type is forbidden. Vehicle type: " + vehicleType.name() + " Wind speed: " + windSpeed + " m/s");
        }
        if (windSpeed >= 10 && windSpeed < 20) {
            return businessRules.getWsefFee();
        }
        return 0.0;
    }

    private double calculateWeatherPhenomenonExtraFee(String phenomenon, VehicleType vehicleType, BusinessRule businessRules) {
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
            return businessRules.getWpefSnowOrSleet();
        }
        if (phenomenonLower.contains("rain")) {
            return businessRules.getWpefRain();
        }
        return 0.0;
    }
}