package com.sunjintong.secureservice.entity.ticket;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "support_tickets", uniqueConstraints = @UniqueConstraint(
        name = "uk_ticket_reporter_request", columnNames = {"reporter_id", "request_id"}))
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(nullable = false, length = 4000)
    private String draft;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Ticket() {
    }

    public Ticket(Long reporterId, String requestId, String draft) {
        this.reporterId = reporterId;
        this.requestId = requestId;
        this.draft = draft;
        this.status = "SUBMITTED";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getDraft() { return draft; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
