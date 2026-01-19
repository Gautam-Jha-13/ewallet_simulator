package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    /**
     * Writes audit logs in a NEW transaction.
     * Must never affect main business flow.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            User user,
            String action,
            String status,
            BigDecimal oldBal,
            BigDecimal newBal
    ) {
        try {
            AuditLog audit = new AuditLog();

            if (user != null) {
                audit.setUserId(user.getId());
                audit.setUsername(user.getEmail());
            } else {
                audit.setUsername("Unknown");
            }

            audit.setActionType(action);
            audit.setStatus(status);
            audit.setOldBalance(oldBal);
            audit.setNewBalance(newBal);

            auditLogRepository.save(audit);

            log.debug(
                "Audit log saved: user={}, action={}, status={}",
                audit.getUsername(),
                action,
                status
            );

        } catch (Exception e) {
            // 🔒 FAIL-SAFE: Audit logging must never break core logic
            log.error(
                "Failed to write audit log (ignored to preserve flow)",
                e
            );
        }
    }
}
