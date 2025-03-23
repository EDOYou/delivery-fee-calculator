package com.calculation.fee.delivery.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_rules")
@Data
public class BusinessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //rbf related fees
    private double tallinnCarBaseFee;
    private double tallinnScooterBaseFee;
    private double tallinnBikeBaseFee;

    private double tartuCarBaseFee;
    private double tartuScooterBaseFee;
    private double tartuBikeBaseFee;

    private double parnuCarBaseFee;
    private double parnuScooterBaseFee;
    private double parnuBikeBaseFee;

    //atef related fees
    private double atefBelowMinusTen;
    private double atefBelowZero;

    //wsef related fees
    private double wsefFee;

    //wpef related fees
    private double wpefSnowOrSleet;
    private double wpefRain;

    private LocalDateTime timestamp;
}