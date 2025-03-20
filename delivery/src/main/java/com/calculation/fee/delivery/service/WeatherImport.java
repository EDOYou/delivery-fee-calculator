package com.calculation.fee.delivery.service;

import com.calculation.fee.delivery.model.City;
import com.calculation.fee.delivery.model.Weather;
import com.calculation.fee.delivery.repository.WeatherRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.calculation.fee.delivery.model.xml.Observation;
import com.calculation.fee.delivery.model.xml.Station;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherImport {

    private static final List<String> REQUIRED_STATIONS = Arrays.stream(City.values())
            .map(City::getStationName)
            .collect(Collectors.toList());

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;

    @Value("${weather.api.url:https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php}")
    private String weatherApiUrl;

    public WeatherImport(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
        this.restTemplate = new RestTemplate();
        this.xmlMapper = new XmlMapper();
    }

    @Scheduled(cron = "${weather.import.cron:0 15 * * * *}")
    public void importWeatherData() {
        try {
            log.info("Starting weather data import from {}", weatherApiUrl);
            String xmlData = restTemplate.getForObject(weatherApiUrl, String.class);
            log.info("Raw XML response: {}", xmlData);
            if (xmlData == null) {
                log.error("Failed to fetch weather data: response is null");
                return;
            }

            Observation observation = xmlMapper.readValue(xmlData, Observation.class);
            log.info("Parsed observation: stations count = {}", observation.getStations() != null ? observation.getStations().size() : 0);
            if (observation == null || observation.getStations() == null) {
                log.error("Failed to parse weather data: observation or stations list is null");
                return;
            }

            LocalDateTime observationTime = parseTimestamp(observation.getTimestamp());

            for (Station station : observation.getStations()) {
                if (station != null && REQUIRED_STATIONS.contains(station.getName())) {
                    saveWeatherData(station, observationTime);
                }
            }

            log.info("Weather data import completed successfully");
        } catch (Exception e) {
            log.error("Error during weather data import: {}", e.getMessage(), e);
        }
    }

    private void saveWeatherData(Station station, LocalDateTime timestamp) {
        Weather weatherData = new Weather();
        weatherData.setStationName(station.getName());
        weatherData.setWmoCode(station.getWmoCode());

        String tempStr = station.getAirtemperature();
        Double airTemperature = parseNumericValue(tempStr);
        weatherData.setAirTemperature(airTemperature);

        String windStr = station.getWindspeed();
        Double windSpeed = parseNumericValue(windStr);
        weatherData.setWindSpeed(windSpeed);

        weatherData.setWeatherPhenomenon(station.getPhenomenon());
        weatherData.setTimestamp(timestamp);

        weatherRepository.save(weatherData);
        log.info("Saved weather data for station: {}", station.getName());
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            log.warn("Timestamp is null or empty, using current time");
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        try {
            long epochSeconds = Long.parseLong(timestamp);
            return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse timestamp '{}', using current time: {}", timestamp, e.getMessage());
            return LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    private Double parseNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            String[] parts = value.split("[ /><]");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    return Double.parseDouble(part.trim());
                }
            }
            return null;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric value from '{}': {}", value, e.getMessage());
            return null;
        }
    }
}