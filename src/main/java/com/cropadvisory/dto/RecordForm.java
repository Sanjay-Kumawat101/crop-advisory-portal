package com.cropadvisory.dto;

import com.cropadvisory.model.AdvisoryRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form object for creating an advisory record. */
public class RecordForm {

    @NotBlank(message = "Village is required")
    @Size(max = 80, message = "Village must be at most 80 characters")
    private String village;

    @NotBlank(message = "Crop is required")
    @Size(max = 50, message = "Crop must be at most 50 characters")
    private String crop;

    @NotBlank(message = "Problem title is required")
    @Size(max = 120, message = "Problem title must be at most 120 characters")
    private String problemTitle;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static RecordForm from(AdvisoryRecord record) {
        RecordForm form = new RecordForm();
        form.setVillage(record.getVillage());
        form.setCrop(record.getCrop());
        form.setProblemTitle(record.getProblemTitle());
        form.setDescription(record.getDescription());
        return form;
    }
}
