package com.calculation.fee.delivery.model;

import com.calculation.fee.delivery.exception.InvalidCityName;
import lombok.Getter;

@Getter
public enum City {
    TALLINN("Tallinn-Harku"),
    TARTU("Tartu-Tõravere"),
    PARNU("Pärnu");

    private final String stationName;

    City(String stationName) {
        this.stationName = stationName;
    }

    public static City fromString(String cityName) {
        try {
            return City.valueOf(cityName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCityName("City name should be one of these: Tallinn-Harku, Tartu-Tõravere or Pärnu: " + e.getMessage());
        }
    }
}