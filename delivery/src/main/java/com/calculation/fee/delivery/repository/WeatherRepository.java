package com.calculation.fee.delivery.repository;

import com.calculation.fee.delivery.model.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    @Query("SELECT w FROM Weather w WHERE w.stationName = :stationName ORDER BY w.timestamp DESC LIMIT 1")
    Optional<Weather> getLatestWeatherForStation(String stationName);

    @Query("SELECT w FROM Weather w WHERE w.stationName = :stationName AND w.timestamp <= :datetime ORDER BY w.timestamp DESC LIMIT 1")
    Optional<Weather> getWeatherForStationAtOrBefore(String stationName, LocalDateTime datetime);
}