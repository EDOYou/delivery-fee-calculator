package com.calculation.fee.delivery.controller;

import com.calculation.fee.delivery.exception.InvalidCityName;
import com.calculation.fee.delivery.exception.InvalidVehicleType;
import com.calculation.fee.delivery.exception.UsageForbiddenException;
import com.calculation.fee.delivery.model.City;
import com.calculation.fee.delivery.model.VehicleType;
import com.calculation.fee.delivery.service.DeliveryFee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class DeliveryFeeController {

    private final DeliveryFee deliveryFee;

    public DeliveryFeeController(DeliveryFee deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    /**
     * Calculates the delivery fee based on the city, vehicle type, and an optional datetime.
     * <p>
     * This endpoint calculates the total delivery fee for a food courier based on the regional base fee (RBF)
     * and extra fees for weather conditions (ATEF, WSEF, WPEF) in the specified city. The calculation uses the
     * weather data closest to the specified datetime (if provided) or the latest weather data if no datetime is given.
     * If the weather conditions forbid the usage of the vehicle type (e.g., high wind speed for Bike), an error is returned.
     * </p>
     * <p>
     * Example: GET /api/delivery-fee?city=Tallinn&vehicleType=Car&datetime=2025-03-22T10:00:00
     * </p>
     *
     * @param city        The city for delivery (e.g., Tallinn, Tartu, Pärnu). Required.
     * @param vehicleType The vehicle type (e.g., Car, Scooter, Bike). Required.
     * @param datetime    The datetime for which to calculate the fee (optional, format: yyyy-MM-dd'T'HH:mm:ss).
     * @return A ResponseEntity containing a map with the calculated fee and currency (EUR).
     *         - On success: 200 OK with { "fee": 5.0, "currency": "EUR" }
     *         - On invalid input: 400 Bad Request with { "error": "Error message" }
     *         - On forbidden usage: 403 Forbidden with { "error": "Usage forbidden message" }
     *         - On no weather data: 503 Service Unavailable with { "error": "No weather data available" }
     *         - On unexpected error: 500 Internal Server Error with { "error": "An unexpected error occurred" }
     */
    @GetMapping(value = "/delivery-fee")
    public ResponseEntity<Map<String, Object>> calculateDeliveryFee(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "vehicleType", required = false) String vehicleType,
            @RequestParam(value = "datetime", required = false) String datetime) {
        try {
            if (city == null || city.trim().isEmpty()) {
                log.error("Missing required parameter: city");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Required parameter is missing: city. Provide a city type: Tallinn, Tartu or Pärnu");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (vehicleType == null || vehicleType.trim().isEmpty()) {
                log.error("Missing required parameter: vehicleType");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Required parameter is missing: vehicleType. Provide a vehicle type: Car, Scooter or Bike");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            LocalDateTime parsedDateTime = null;
            if (datetime != null && !datetime.trim().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    parsedDateTime = LocalDateTime.parse(datetime, formatter);
                } catch (DateTimeParseException e) {
                    log.error("Invalid datetime format: {}", datetime);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid datetime format. Use yyyy-MM-dd'T'HH:mm:ss (e.g., 2025-03-22T10:00:00)");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            log.info("Received request to calculate delivery fee for city: {}, vehicleType: {}", city, vehicleType);

            City parsedCity = City.fromString(city);
            VehicleType parsedVehicleType = VehicleType.fromString(vehicleType);
            double fee = deliveryFee.calculateDeliveryFee(parsedCity, parsedVehicleType, parsedDateTime);

            Map<String, Object> response = new HashMap<>();
            response.put("fee", fee);
            response.put("currency", "EUR");
            return ResponseEntity.ok(response);

        } catch (InvalidCityName | InvalidVehicleType e) {
            log.error("Invalid input: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (UsageForbiddenException e) {
            log.error("Usage forbidden: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (IllegalStateException e) {
            log.error("There is no weather data curently: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during fee calculation: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}