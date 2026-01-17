package com.ewallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username") // stored for readability in logs
    private String username;

    @Column(name = "action_type", nullable = false)
    private String actionType; // TRANSFER, RECEIVE, LOGIN

    @Column(nullable = false)
    private String status; // SUCCESS, FAILURE

    @Column(name = "old_balance")
    private BigDecimal oldBalance;

    @Column(name = "new_balance")
    private BigDecimal newBalance;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}