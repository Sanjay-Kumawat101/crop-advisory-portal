package com.cropadvisory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advisory_records")
public class AdvisoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id")
    private User farmer;

    @Column(nullable = false, length = 80)
    private String village;

    @Column(nullable = false, length = 50)
    private String crop;

    @Column(name = "problem_title", nullable = false, length = 120)
    private String problemTitle;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.SUBMITTED;

    @Column(length = 2000)
    private String advice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private User expert;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AdvisoryRecord() {
    }

    public AdvisoryRecord(User farmer, String village, String crop, String problemTitle, String description) {
        this.farmer = farmer;
        this.village = village;
        this.crop = crop;
        this.problemTitle = problemTitle;
        this.description = description;
        this.status = Status.SUBMITTED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = Status.SUBMITTED;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public User getExpert() { return expert; }
    public void setExpert(User expert) { this.expert = expert; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
