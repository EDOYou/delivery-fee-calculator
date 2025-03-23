package com.calculation.fee.delivery.util;

import com.calculation.fee.delivery.model.BusinessRule;

import java.time.LocalDateTime;

public class TestUtils {

    private static LocalDateTime baseTimestamp = LocalDateTime.now();

    public static BusinessRule createBusinessRule() {
        BusinessRule businessRule = new BusinessRule();
        businessRule.setTallinnCarBaseFee(4.0);
        businessRule.setTallinnScooterBaseFee(3.5);
        businessRule.setTallinnBikeBaseFee(3.0);
        businessRule.setTartuCarBaseFee(3.5);
        businessRule.setTartuScooterBaseFee(3.0);
        businessRule.setTartuBikeBaseFee(2.5);
        businessRule.setParnuCarBaseFee(3.0);
        businessRule.setParnuScooterBaseFee(2.5);
        businessRule.setParnuBikeBaseFee(2.0);
        businessRule.setAtefBelowMinusTen(1.0);
        businessRule.setAtefBelowZero(0.5);
        businessRule.setWsefFee(0.5);
        businessRule.setWpefSnowOrSleet(1.0);
        businessRule.setWpefRain(0.5);
        businessRule.setTimestamp(baseTimestamp.minusHours(3));
        return businessRule;
    }
}
