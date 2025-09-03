package fintech2.easypay.transfer.entity;

public enum RepeatCycle {
    DAILY("매일"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    YEARLY("매년");
    
    private final String description;
    
    RepeatCycle(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}