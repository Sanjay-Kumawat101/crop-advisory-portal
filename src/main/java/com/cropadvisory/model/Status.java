package com.cropadvisory.model;

public enum Status {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    ADVISED("Advised"),
    CLOSED("Closed");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
