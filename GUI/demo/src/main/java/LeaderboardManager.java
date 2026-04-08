package application;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardManager {

    private static final String FILE_NAME = "leaderboard.txt";

    public static List<LeaderboardEntry> loadLeaderboard() {
        List<LeaderboardEntry> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    int savings = Integer.parseInt(parts[1]);
                    list.add(new LeaderboardEntry(name, savings));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sortDescending(list);
        return list;
    }

    public static void addEntry(String name, int savings) {
        List<LeaderboardEntry> list = loadLeaderboard();
        list.add(new LeaderboardEntry(name, savings));
        sortDescending(list);

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (LeaderboardEntry entry : list) {
                pw.println(entry.getName() + "," + entry.getSavings());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sortDescending(List<LeaderboardEntry> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).getSavings() > list.get(i).getSavings()) {
                    LeaderboardEntry temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }
    }
}