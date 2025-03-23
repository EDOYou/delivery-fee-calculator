package com.calculation.fee.delivery.controller;

import com.calculation.fee.delivery.model.BusinessRule;
import com.calculation.fee.delivery.repository.BusinessRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/business-rules")
@Slf4j
public class BusinessRuleController {

    private final BusinessRuleRepository businessRuleRepository;

    public BusinessRuleController(BusinessRuleRepository businessRuleRepository) {
        this.businessRuleRepository = businessRuleRepository;
    }

    /**
     * Creates a new business rule
     */
    @PostMapping
    public ResponseEntity<BusinessRule> createBusinessRule(@RequestBody BusinessRule businessRule) {
        try {
            log.info("Creating new business rule");
            businessRule.setTimestamp(LocalDateTime.now());
            BusinessRule savedRule = businessRuleRepository.save(businessRule);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
        } catch (Exception e) {
            log.error("Error creating business rule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create business rule", e);
        }
    }

    /**
     * Retrieves all business rules
     */
    @GetMapping
    public ResponseEntity<List<BusinessRule>> getAllBusinessRules() {
        try {
            log.info("Retrieving all business rules");
            List<BusinessRule> rules = businessRuleRepository.findAll();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            log.error("Error retrieving business rules: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve business rules", e);
        }
    }

    /**
     * Retrieves a business rule by ID
     */
    @GetMapping(value = "/{id}")
    public ResponseEntity<BusinessRule> getBusinessRuleById(@PathVariable Long id) {
        try {
            log.info("Retrieving business rule with ID: {}", id);
            Optional<BusinessRule> businessRule = businessRuleRepository.findById(id);
            if (businessRule.isEmpty()) {
                log.warn("Business rule with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(businessRule.get());
        } catch (Exception e) {
            log.error("Error retrieving business rule with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve business rule", e);
        }
    }

    /**
     * Updates an existing business rule
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<BusinessRule> updateBusinessRule(@PathVariable Long id, @RequestBody BusinessRule updatedRule) {
        try {
            log.info("Updating business rule with ID: {}", id);
            Optional<BusinessRule> existingRule = businessRuleRepository.findById(id);
            if (existingRule.isEmpty()) {
                log.warn("Business rule with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }

            BusinessRule rule = existingRule.get();
            rule.setTallinnCarBaseFee(updatedRule.getTallinnCarBaseFee());
            rule.setTallinnScooterBaseFee(updatedRule.getTallinnScooterBaseFee());
            rule.setTallinnBikeBaseFee(updatedRule.getTallinnBikeBaseFee());
            rule.setTartuCarBaseFee(updatedRule.getTartuCarBaseFee());
            rule.setTartuScooterBaseFee(updatedRule.getTartuScooterBaseFee());
            rule.setTartuBikeBaseFee(updatedRule.getTartuBikeBaseFee());
            rule.setParnuCarBaseFee(updatedRule.getParnuCarBaseFee());
            rule.setParnuScooterBaseFee(updatedRule.getParnuScooterBaseFee());
            rule.setParnuBikeBaseFee(updatedRule.getParnuBikeBaseFee());
            rule.setAtefBelowMinusTen(updatedRule.getAtefBelowMinusTen());
            rule.setAtefBelowZero(updatedRule.getAtefBelowZero());
            rule.setWsefFee(updatedRule.getWsefFee());
            rule.setWpefSnowOrSleet(updatedRule.getWpefSnowOrSleet());
            rule.setWpefRain(updatedRule.getWpefRain());
            rule.setTimestamp(LocalDateTime.now());

            BusinessRule savedRule = businessRuleRepository.save(rule);
            return ResponseEntity.ok(savedRule);
        } catch (Exception e) {
            log.error("Error updating business rule with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update business rule", e);
        }
    }

    /**
     * Deletes a business rule by ID
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Map<String, String>> deleteBusinessRule(@PathVariable Long id) {
        try {
            log.info("Deleting business rule with ID: {}", id);
            if (!businessRuleRepository.existsById(id)) {
                log.warn("Business rule with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }
            businessRuleRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Business rule with ID " + id + " deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting business rule with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete business rule", e);
        }
    }
}