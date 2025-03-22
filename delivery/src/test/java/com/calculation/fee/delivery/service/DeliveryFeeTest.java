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
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        weather = new Weather();
        weather.setStationName("Tallinn-Harku");
        weather.setWmoCode("26128");
        weather.setTimestamp(testDateTime);
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
        double fee = deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.CAR, null);

        //then
        assertEquals(4.0, fee);
    }

    @Test
    void testCalculateFeeInTallinnWithCarAndClearWeather_Historical() {
        //given
        weather.setStationName(City.TALLINN.getStationName());
        weather.setAirTemperature(-5.0);
        weather.setWindSpeed(12.0);
        weather.setWeatherPhenomenon("Light snow");
        when(weatherRepository.getWeatherForStationAtOrBefore(City.TALLINN.getStationName(), testDateTime))
                .thenReturn(Optional.of(weather));

        //when
        double fee = deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.CAR, testDateTime);

        //then
        assertEquals(6.0, fee);
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
        double fee = deliveryFee.calculateDeliveryFee(City.TARTU, VehicleType.SCOOTER, null);

        //then
        assertEquals(3.0 + 1.0 + 1.0, fee);
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
        double fee = deliveryFee.calculateDeliveryFee(City.PARNU, VehicleType.BIKE, null);

        //then
        assertEquals(2.0 + 0.5 + 0.5, fee);
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
            deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.SCOOTER, null);
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
            deliveryFee.calculateDeliveryFee(City.TARTU, VehicleType.BIKE, null);
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
            deliveryFee.calculateDeliveryFee(City.PARNU, VehicleType.CAR, null);
        });

        //then
        assertEquals("No weather data available for Pärnu", exception.getMessage());
    }

    @Test
    void testCalculateFeeWithNoWeatherDataAtSpecifiedTimeThrowsException() {
        //given
        when(weatherRepository.getWeatherForStationAtOrBefore(City.PARNU.getStationName(), testDateTime))
                .thenReturn(Optional.empty());

        //when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deliveryFee.calculateDeliveryFee(City.PARNU, VehicleType.CAR, testDateTime);
        });

        //then
        assertEquals("No weather data available for Pärnu at or before " + testDateTime, exception.getMessage());
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
        double fee = deliveryFee.calculateDeliveryFee(City.TALLINN, VehicleType.CAR, null);

        //then
        assertEquals(4.0, fee);
    }
}