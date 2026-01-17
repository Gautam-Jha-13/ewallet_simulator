package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String status, BigDecimal oldBal, BigDecimal newBal) {
        try {
            AuditLog log = new AuditLog();
            if (user != null) {
                log.setUserId(user.getId());
                log.setUsername(user.getEmail());
            } else {
                log.setUsername("Unknown");
            }
            
            log.setActionType(action);
            log.setStatus(status);
            log.setOldBalance(oldBal);
            log.setNewBalance(newBal);
            

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Failsafe: don't break application if logging fails
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }
}
