package com.cropadvisory.controller;

import com.cropadvisory.model.AdvisoryRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecordForm {

    @NotBlank(message = "Village is required")
    @Size(max = 80)
    private String village;

    @NotBlank(message = "Crop is required")
    @Size(max = 50)
    private String crop;

    @NotBlank(message = "Problem title is required")
    @Size(max = 120)
    private String problemTitle;

    @Size(max = 1000, message = "Description must be 1000 characters or less")
    private String description;

    public static RecordForm from(AdvisoryRecord r) {
        RecordForm f = new RecordForm();
        f.village = r.getVillage();
        f.crop = r.getCrop();
        f.problemTitle = r.getProblemTitle();
        f.description = r.getDescription();
        return f;
    }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}