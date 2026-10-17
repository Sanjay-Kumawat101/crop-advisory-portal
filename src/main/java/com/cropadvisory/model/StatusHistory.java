package com.cropadvisory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id")
    private AdvisoryRecord record;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 15)
    private Status oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 15)
    private Status newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected StatusHistory() {
    }

    public StatusHistory(AdvisoryRecord record, Status oldStatus, Status newStatus, User changedBy) {
        this.record = record;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AdvisoryRecord getRecord() { return record; }
    public Status getOldStatus() { return oldStatus; }
    public Status getNewStatus() { return newStatus; }
    public User getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
