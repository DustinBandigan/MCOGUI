package application;

public class LeaderboardEntry {
    private final String name;
    private final int savings;

    public LeaderboardEntry(String name, int savings) {
        this.name = name;
        this.savings = savings;
    }

    public String getName() {
        return name;
    }

    public int getSavings() {
        return savings;
    }
}