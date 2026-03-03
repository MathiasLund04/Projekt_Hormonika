package Enums;

public enum Haircuts {
    MANCUT("Mande klipning", 30),
    WOMANCUT("Kvinde klipning", 45),
    CHILDCUT("Børne klipning", 30),
    COLOUR("Farve", 120),
    PERM("Permanent", 120),
    BEARD("Skæg klipning", 30),
    OTHER("Andet", 60);

    private final String description;
    private final int time;
    Haircuts(String description, int time) {
        this.description = description;
        this.time = time;
    }
    public String getDescription() {
        return description;
    }
    public int getTime() {
        return time;
    }
}
