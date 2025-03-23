# Delivery Fee Calculation Service

## Overview
A Spring Boot app to calculate delivery fees for Tallinn, Tartu, and Pärnu using Car, Scooter, or Bike. Fees are based on a regional base fee (RBF) plus extra fees for air temperature (ATEF), wind speed (WSEF), and weather phenomena (WPEF)

### Features
- Calculates fees using weather data
- Configurable business rules via REST API

## Prerequisites
- Java 17
- Maven
- Database: H2 (in-memory, used for testing)

## Setup
1. **Clone the Repo**:
   ```bash
   git clone git@github.com:EDOYou/delivery-fee-calculator.git
   cd delivery-fee-calculation
   ```

# API Endpoints
## Delivery Fee Calculation
### GET /api/delivery-fee
- Calculates the delivery fee for a given city and vehicle type
- Query Parameters:
  - city: Tallinn, Tartu, or Pärnu
  - vehicleType: Car, Scooter, or Bike
  - datetime (optional): Format yyyy-MM-dd'T'HH:mm:ss (e.g., 2025-03-22T10:00:00)
- Example:
    ```bash
    curl "{host}/api/delivery-fee?city=Tallinn&vehicleType=Car"
    ```
## Manage Business Rules
### POST /api/business-rules

- Creates a new business rule
- Request Body:
```json
{
  "tallinnCarBaseFee": 4.0,
  "tallinnScooterBaseFee": 3.5,
  "tallinnBikeBaseFee": 3.0,
  "tartuCarBaseFee": 3.5,
  "tartuScooterBaseFee": 3.0,
  "tartuBikeBaseFee": 2.5,
  "parnuCarBaseFee": 3.0,
  "parnuScooterBaseFee": 2.5,
  "parnuBikeBaseFee": 2.0,
  "atefBelowMinusTen": 1.0,
  "atefBelowZero": 0.5,
  "wsefFee": 0.5,
  "wpefSnowOrSleet": 1.0,
  "wpefRain": 0.5
}
```
- **Response**: `201 Created`

### GET /api/business-rules
- Lists all business rules
- **Response**:
```json
[{ "id": 1, "tallinnCarBaseFee": 4.0, ... }]
```

### GET /api/business-rules/{id}
- Retrieves a business rule by ID
- **Response**: `200 OK` or `404 Not Found`

### PUT /api/business-rules/{id}
- Updates a business rule
- **Response**: `200 OK` or `404 Not Found`

### DELETE /api/business-rules/{id}
- Deletes a business rule
- **Response**: `204 No Content` or `404 Not Found`
