package fintech2.easypay.transfer.entity;

public enum ScheduleType {
    ONE_TIME("일회성"),
    RECURRING("반복");
    
    private final String description;
    
    ScheduleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}