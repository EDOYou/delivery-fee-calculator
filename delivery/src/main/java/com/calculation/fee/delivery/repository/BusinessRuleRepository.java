package com.calculation.fee.delivery.repository;

import com.calculation.fee.delivery.model.BusinessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Long> {

    /**
     * Retrieves the latest business rule
     */
    Optional<BusinessRule> findFirstByOrderByTimestampDesc();

    /**
     * Retrieves the business rule that was valid at or before the specified datetime
     */
    Optional<BusinessRule> findFirstByTimestampLessThanEqualOrderByTimestampDesc(LocalDateTime datetime);
}