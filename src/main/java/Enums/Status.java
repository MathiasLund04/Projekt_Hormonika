package Enums;

public enum Status {
    ACTIVE("Afventer"),
    CANCELLED("Aflyst"),
    COMPLETED("Færdig");
    
    private final String description;

    Status(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

}
