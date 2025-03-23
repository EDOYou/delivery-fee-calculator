package com.calculation.fee.delivery.controller;

import com.calculation.fee.delivery.model.BusinessRule;
import com.calculation.fee.delivery.model.Weather;
import com.calculation.fee.delivery.repository.BusinessRuleRepository;
import com.calculation.fee.delivery.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static com.calculation.fee.delivery.util.TestUtils.createBusinessRule;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryFeeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private BusinessRuleRepository businessRuleRepository;

    private LocalDateTime baseTimestamp = LocalDateTime.now();;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();
        businessRuleRepository.deleteAll();

        BusinessRule businessRule = createBusinessRule();
        businessRuleRepository.save(businessRule);

        Weather tallinnWeather = new Weather();
        tallinnWeather.setStationName("Tallinn-Harku");
        tallinnWeather.setWmoCode("26128");
        tallinnWeather.setAirTemperature(-5.0);
        tallinnWeather.setWindSpeed(12.0);
        tallinnWeather.setWeatherPhenomenon("Light snow");
        tallinnWeather.setTimestamp(LocalDateTime.now());
        weatherRepository.save(tallinnWeather);

        Weather tartuWeather = new Weather();
        tartuWeather.setStationName("Tartu-Tõravere");
        tartuWeather.setWmoCode("26242");
        tartuWeather.setAirTemperature(2.0);
        tartuWeather.setWindSpeed(5.0);
        tartuWeather.setWeatherPhenomenon("Clear");
        tartuWeather.setTimestamp(baseTimestamp);
        weatherRepository.save(tartuWeather);

        Weather parnuWeather = new Weather();
        parnuWeather.setStationName("Pärnu");
        parnuWeather.setWmoCode("26038");
        parnuWeather.setAirTemperature(-15.0);
        parnuWeather.setWindSpeed(8.0);
        parnuWeather.setWeatherPhenomenon("Rain");
        parnuWeather.setTimestamp(baseTimestamp);
        weatherRepository.save(parnuWeather);
    }

    @Test
    void testCalculateFeeTallinnCarSuccess() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tallinn&vehicleType=Car", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6.0, response.getBody().get("fee"));
        assertEquals("EUR", response.getBody().get("currency"));
    }

    @Test
    void testCalculateFeeTartuScooterSuccess() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tartu&vehicleType=Scooter", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3.0, response.getBody().get("fee"));
        assertEquals("EUR", response.getBody().get("currency"));
    }

    @Test
    void testCalculateFeeParnuBikeSuccess() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Parnu&vehicleType=Bike", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3.5, response.getBody().get("fee"));
        assertEquals("EUR", response.getBody().get("currency"));
    }

    @Test
    void testCalculateFeeMissingCity() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?vehicleType=Car", Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Required parameter is missing: city. Provide a city type: Tallinn, Tartu or Pärnu",
                response.getBody().get("error"));
    }

    @Test
    void testCalculateFeeMissingVehicleType() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tallinn", Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Required parameter is missing: vehicleType. Provide a vehicle type: Car, Scooter or Bike",
                response.getBody().get("error"));
    }

    @Test
    void testCalculateFeeInvalidCity() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Narva&vehicleType=Car", Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((String) response.getBody().get("error")).contains("City name should be one of these"));
    }

    @Test
    void testCalculateFeeForbiddenUsage() {
        Weather tallinnWeather = weatherRepository.getLatestWeatherForStation("Tallinn-Harku").get();
        tallinnWeather.setWindSpeed(25.0);
        weatherRepository.save(tallinnWeather);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tallinn&vehicleType=Bike", Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usage of selected vehicle type is forbidden. Vehicle type: BIKE Wind speed: 25.0 m/s",
                response.getBody().get("error"));
    }

    @Test
    void testCalculateFeeNoWeatherData() {
        weatherRepository.deleteAll();

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tallinn&vehicleType=Car", Map.class);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No weather data available for Tallinn-Harku",
                response.getBody().get("error"));
    }

    @Test
    void testCalculateFeeInvalidVehicleType() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/delivery-fee?city=Tallinn&vehicleType=Truck", Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((String) response.getBody().get("error")).contains("Vehicle type should be only of these: CAR, SCOOTER, BIKE"));
    }
}
