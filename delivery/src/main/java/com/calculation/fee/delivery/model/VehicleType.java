package com.calculation.fee.delivery.model;

import com.calculation.fee.delivery.exception.InvalidVehicleType;

public enum VehicleType {
    CAR,
    SCOOTER,
    BIKE;

    public static VehicleType fromString(String vehicleType) {
        try {
            return VehicleType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidVehicleType("Vehicle type should be only of these: CAR, SCOOTER, BIKE: " + e.getMessage());
        }
    }
}