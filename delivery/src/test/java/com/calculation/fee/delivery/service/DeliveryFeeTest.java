package com.calculation.fee.delivery.service;

import com.calculation.fee.delivery.exception.UsageForbiddenException;
import com.calculation.fee.delivery.model.City;
import com.calculation.fee.delivery.model.VehicleType;
import com.calculation.fee.delivery.model.Weather;
import com.calculation.fee.delivery.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeTest {

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private DeliveryFee deliveryFee;

    private Weather weather;

    @BeforeEach
    void setUp() {
        weather = new Weather();
        weather.setStationName("Tallinn-Harku");
        weather.setWmoCode("26128");
        weather.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testCalculateFeeInTallinnWithCarAndClearWeather() {
        //given
        weather.setStationName(City.TALLINN.getStationName());
        weather.setAirTemperature(5.0);
        weather.setWindSpeed(8.0);
        weather.setWeatherPhenomenon("Clear");
        when(weatherRepository.getLatestWeatherForStation(City.TALLINN.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        double fee = deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.CAR);

        //then
        assertEquals(4.0, fee, 0.001);
    }

    @Test
    void testCalculateFeeInTartuWithScooterAndSnow() {
        //given
        weather.setStationName(City.TARTU.getStationName());
        weather.setAirTemperature(-15.0);
        weather.setWindSpeed(5.0);
        weather.setWeatherPhenomenon("Heavy snow");
        when(weatherRepository.getLatestWeatherForStation(City.TARTU.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        double fee = deliveryFee.calculateDeliveryFee(City.TARTU, VehicleType.SCOOTER);

        //then
        assertEquals(3.0 + 1.0 + 1.0, fee, 0.001);
    }

    @Test
    void testCalculateFeeInParnuWithBikeAndRain() {
        //given
        weather.setStationName(City.PARNU.getStationName());
        weather.setAirTemperature(2.0);
        weather.setWindSpeed(15.0);
        weather.setWeatherPhenomenon("Light rain");
        when(weatherRepository.getLatestWeatherForStation(City.PARNU.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        double fee = deliveryFee.calculateDeliveryFee(City.PARNU, VehicleType.BIKE);

        //then
        assertEquals(2.0 + 0.5 + 0.5, fee, 0.001);
    }

    @Test
    void testCalculateFeeWithScooterAndHighWindSpeedThrowsException() {
        //given
        weather.setStationName(City.TALLINN.getStationName());
        weather.setAirTemperature(0.0);
        weather.setWindSpeed(25.0);
        weather.setWeatherPhenomenon("Clear");
        when(weatherRepository.getLatestWeatherForStation(City.TALLINN.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        UsageForbiddenException exception = assertThrows(UsageForbiddenException.class, () -> {
            deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER);
        });

        //then
        assertEquals("Usage of selected vehicle type is forbidden. Vehicle type: SCOOTER Wind speed: 25.0 m/s", exception.getMessage());
    }

    @Test
    void testCalculateFeeWithBikeAndThunderWeatherThrowsException() {
        //given
        weather.setStationName(City.TARTU.getStationName());
        weather.setAirTemperature(10.0);
        weather.setWindSpeed(5.0);
        weather.setWeatherPhenomenon("Thunderstorm");
        when(weatherRepository.getLatestWeatherForStation(City.TARTU.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        UsageForbiddenException exception = assertThrows(UsageForbiddenException.class, () -> {
            deliveryFee.calculateDeliveryFee(City.TARTU, VehicleType.BIKE);
        });

        //then
        assertEquals("Usage of selected vehicle type is forbidden. Vehicle type: BIKE. Phenomenon: Thunderstorm", exception.getMessage());
    }

    @Test
    void testCalculateFeeWithNoWeatherDataThrowsException() {
        //given
        when(weatherRepository.getLatestWeatherForStation(City.PARNU.getStationName()))
                .thenReturn(Optional.empty());

        //when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deliveryFee.calculateDeliveryFee(City.PARNU, VehicleType.CAR);
        });

        //then
        assertEquals("No weather data available for Pärnu", exception.getMessage());
    }

    @Test
    void testCalculateFeeWithNullWeatherValues() {
        //given
        weather.setStationName(City.TALLINN.getStationName());
        weather.setAirTemperature(null);
        weather.setWindSpeed(null);
        weather.setWeatherPhenomenon(null);
        when(weatherRepository.getLatestWeatherForStation(City.TALLINN.getStationName()))
                .thenReturn(Optional.of(weather));

        //when
        double fee = deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.CAR);

        //then
        assertEquals(4.0, fee, 0.001);
    }
}