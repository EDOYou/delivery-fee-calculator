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

    @GetMapping(value = "/delivery-fee")
    public ResponseEntity<Map<String, Object>> calculateDeliveryFee(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "vehicleType", required = false) String vehicleType) {
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

            log.info("Received request to calculate delivery fee for city: {}, vehicleType: {}", city, vehicleType);

            City parsedCity = City.fromString(city);
            VehicleType parsedVehicleType = VehicleType.fromString(vehicleType);
            double fee = deliveryFee.calculateDeliveryFee(parsedCity, parsedVehicleType);

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