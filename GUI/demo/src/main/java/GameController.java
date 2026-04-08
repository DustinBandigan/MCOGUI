package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameController {

    @FXML private Label playerNameLabel;
    @FXML private Label dayLabel;
    @FXML private Label savingsLabel;
    @FXML private Label waterLabel;

    @FXML private Label plantTypeLabel;
    @FXML private Label stageLabel;
    @FXML private Label wateredLabel;
    @FXML private Label readyHarvestLabel;

    @FXML private GridPane farmGrid;
    @FXML private VBox plantInfoBox;
    @FXML private Button excavateButton;

    private int currentDay = 1;
    private int savings = 1000;
    private int water = 10;
    private final int maxWater = 10;

    private boolean meteorTriggered = false;
    private boolean excavateMode = false;
    private int excavationsToday = 0;
    private boolean scoreSaved = false;

    private static final int SIZE = 10;
    private static final int MAX_EXCAVATIONS_PER_DAY = 5;
    private static final int EXCAVATE_COST = 500;
    private static final int REFILL_COST = 100;
    private static final int FERTILIZER_COST = 200;
    private static final int FERTILIZER_DAYS = 3;

    private final Button[][] tileButtons = new Button[SIZE][SIZE];

    private final char[][] soil = {
            {'l','l','l','l','l','l','l','l','l','l'},
            {'g','s','g','g','s','s','g','g','s','g'},
            {'l','l','l','l','l','l','l','l','l','l'},
            {'s','s','s','g','g','g','g','s','s','s'},
            {'s','l','s','g','g','g','g','s','l','s'},
            {'s','l','s','g','g','g','g','s','l','s'},
            {'s','s','s','g','g','g','g','s','s','s'},
            {'l','l','l','l','l','l','l','l','l','l'},
            {'g','s','g','g','s','s','g','g','s','g'},
            {'l','l','l','l','l','l','l','l','l','l'}
    };

    private final boolean[][] meteorTiles = new boolean[SIZE][SIZE];
    private final boolean[][] permanentFertilizer = new boolean[SIZE][SIZE];
    private final boolean[][] wateredTiles = new boolean[SIZE][SIZE];
    private final int[][] fertilizerDays = new int[SIZE][SIZE];

    private final String[][] plantedName = new String[SIZE][SIZE];
    private final int[][] plantedYield = new int[SIZE][SIZE];
    private final String[][] plantedPreferredSoil = new String[SIZE][SIZE];
    private final int[][] plantedGrowth = new int[SIZE][SIZE];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private final Map<String, char[]> growthMap = new HashMap<>();

    public void initialize() {
        initializeGrowthStages();

        currentDay = 1;
        savings = 1000;
        water = 10;
        meteorTriggered = false;
        excavateMode = false;
        excavationsToday = 0;
        scoreSaved = false;

        selectedRow = -1;
        selectedCol = -1;

        dayLabel.setText("Day " + currentDay);
        savingsLabel.setText("Savings: " + savings);
        waterLabel.setText("Water: " + water + "/" + maxWater);

        plantInfoBox.setVisible(false);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                meteorTiles[r][c] = false;
                permanentFertilizer[r][c] = false;
                wateredTiles[r][c] = false;
                fertilizerDays[r][c] = 0;

                plantedName[r][c] = "";
                plantedYield[r][c] = 0;
                plantedPreferredSoil[r][c] = "";
                plantedGrowth[r][c] = 0;
            }
        }

        createGrid();
        updateExcavateButtonState();
    }

    private void initializeGrowthStages() {
        // S = Seedling, D = Dormant, E = Energizing, L = Low Productive, H = High Productive, M = Fully Mature

        growthMap.put("Turnip", new char[]{
                'S','S','S',
                'D',
                'L','L','L','L',
                'H',
                'M'
        });

        growthMap.put("Wheat", new char[]{
                'S',
                'L','L','L','L',
                'E','E','E',
                'H','H',
                'E','E','E',
                'M'
        });

        growthMap.put("Thyme", new char[]{
                'S','S','S','S','S','S','S','S','S','S','S','S',
                'M'
        });

        growthMap.put("Potato", new char[]{
                'S','S','S','S','S',
                'E','D','L',
                'E','D','L',
                'E','D','L',
                'E','D','H',
                'E','D','H',
                'E','D','H',
                'M'
        });

        growthMap.put("Tomato", new char[]{
                'S',
                'D','D','D','D','D','D','D','D','D','D','D','D',
                'E','E','E','E','E','E','E','E','E','E',
                'M'
        });
    }

    public void setPlayerName(String name) {
        playerNameLabel.setText(name);
    }

    private void createGrid() {
        farmGrid.getChildren().clear();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button tile = new Button();

                tile.setPrefSize(70, 70);
                tile.setMinSize(70, 70);
                tile.setMaxSize(70, 70);

                final int r = row;
                final int c = col;

                tile.setOnMouseEntered(e -> showTileInfo(r, c));
                tile.setOnMouseExited(e -> plantInfoBox.setVisible(false));

                tile.setOnAction(e -> {
                    if (excavateMode) {
                        handleTileExcavate(r, c);
                    } else {
                        selectTile(r, c);
                    }
                });

                tileButtons[row][col] = tile;
                farmGrid.add(tile, col, row);
            }
        }

        refreshAllTiles();
    }

    private void refreshAllTiles() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                updateTileAppearance(r, c, tileButtons[r][c]);
            }
        }
    }

    private void selectTile(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refreshAllTiles();
    }

    private boolean isSelected(int row, int col) {
        return selectedRow == row && selectedCol == col;
    }

    private boolean hasPlant(int row, int col) {
        return plantedName[row][col] != null && !plantedName[row][col].isEmpty();
    }

    private char[] getPlantStages(String plantName) {
        return growthMap.get(plantName);
    }

    private char getCurrentStageCode(int row, int col) {
        if (!hasPlant(row, col)) return ' ';
        char[] stages = getPlantStages(plantedName[row][col]);
        int index = plantedGrowth[row][col];
        if (index < 0) index = 0;
        if (index >= stages.length) index = stages.length - 1;
        return stages[index];
    }

    private String getStageName(char code) {
        return switch (code) {
            case 'S' -> "Seedling";
            case 'D' -> "Dormant";
            case 'E' -> "Energizing";
            case 'L' -> "Low Productive";
            case 'H' -> "High Productive";
            case 'M' -> "Fully Mature";
            default -> "None";
        };
    }

    private String getSoilName(char soilCode) {
        return switch (soilCode) {
            case 's' -> "Sand";
            case 'g' -> "Gravel";
            default -> "Loam";
        };
    }

    private boolean isPreferredSoil(int row, int col) {
        if (!hasPlant(row, col)) return false;

        String preferred = plantedPreferredSoil[row][col];
        String actual = getSoilName(soil[row][col]).toLowerCase();

        return preferred.equals(actual);
    }

    private boolean hasActiveFertilizer(int row, int col) {
        return permanentFertilizer[row][col] || fertilizerDays[row][col] > 0;
    }

    private void showTileInfo(int row, int col) {
        plantInfoBox.setVisible(true);

        if (meteorTiles[row][col]) {
            plantTypeLabel.setText("Soil Type: Meteorite");
        } else {
            String base = "Soil Type: " + getSoilName(soil[row][col]) + " (" + soil[row][col] + ")";
            if (permanentFertilizer[row][col]) {
                base += " - Permanent Fertilizer";
            } else if (fertilizerDays[row][col] > 0) {
                base += " - Fertilized (" + fertilizerDays[row][col] + " days)";
            }
            plantTypeLabel.setText(base);
        }

        stageLabel.setText("Tile: (" + (col + 1) + "," + (row + 1) + ")");
        wateredLabel.setText("Watered?: " + (wateredTiles[row][col] ? "Y" : "N"));

        if (hasPlant(row, col)) {
            char stageCode = getCurrentStageCode(row, col);
            String info = plantedName[row][col] +
                    " | " + getStageName(stageCode) +
                    " | Stage " + (plantedGrowth[row][col] + 1) + "/" + getPlantStages(plantedName[row][col]).length;
            readyHarvestLabel.setText(info);
        } else {
            readyHarvestLabel.setText("Plant: None");
        }
    }

    private String getTileText(int row, int col) {
        String text;

        if (hasPlant(row, col)) {
            String name = plantedName[row][col];
            text = name.substring(0, Math.min(2, name.length())).toUpperCase();
        } else if (meteorTiles[row][col]) {
            text = "X";
        } else if (permanentFertilizer[row][col]) {
            text = "(" + soil[row][col] + ")";
        } else if (fertilizerDays[row][col] > 0) {
            text = "*" + soil[row][col];
        } else {
            text = String.valueOf(soil[row][col]);
        }

        if (wateredTiles[row][col] && hasPlant(row, col)) {
            text = "[" + text + "]";
        }

        return text;
    }

    private void updateTileAppearance(int row, int col, Button tile) {
        tile.setText(getTileText(row, col));

        if (meteorTiles[row][col]) {
            applyStyle(tile, "#bfbfbf", isSelected(row, col), "black");
            return;
        }

        if (hasPlant(row, col)) {
            char stageCode = getCurrentStageCode(row, col);

            switch (stageCode) {
                case 'S' -> applyStyle(tile, "#8fd14f", isSelected(row, col), "black");
                case 'D' -> applyStyle(tile, "#27aae1", isSelected(row, col), "black");
                case 'E' -> applyStyle(tile, "#7a3db8", isSelected(row, col), "white");
                case 'L' -> applyStyle(tile, "#f2b01e", isSelected(row, col), "black");
                case 'H' -> applyStyle(tile, "#e31c24", isSelected(row, col), "white");
                case 'M' -> applyStyle(tile, "#000000", isSelected(row, col), "white");
                default -> applyStyle(tile, "#f2f2f2", isSelected(row, col), "black");
            }
            return;
        }

        if (permanentFertilizer[row][col] || fertilizerDays[row][col] > 0) {
            applyStyle(tile, "#fff7cc", isSelected(row, col), "black");
        } else {
            applyStyle(tile, "#f2f2f2", isSelected(row, col), "black");
        }
    }

    private void applyStyle(Button tile, String bgColor, boolean selected, String textColor) {
        String borderWidth = selected ? "3" : "1";
        tile.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: " + borderWidth + ";" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + textColor + ";"
        );
    }

    private void updateExcavateButtonState() {
        if (!meteorTriggered) {
            excavateButton.setDisable(true);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: gray;"
            );
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            excavateButton.setDisable(true);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: gray;"
            );
        } else {
            excavateButton.setDisable(false);
            excavateButton.setStyle(
                    "-fx-font-size: 30;" +
                            "-fx-border-color: black;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-color: #efefef;" +
                            "-fx-text-fill: black;"
            );
        }
    }

    private void triggerMeteoriteEvent() {
        int[][] affectedTiles = {
                {1,1},{1,4},{1,5},{1,8},
                {3,3},{3,4},{3,5},{3,6},
                {4,1},{4,3},{4,4},{4,5},{4,6},{4,8},
                {5,1},{5,3},{5,4},{5,5},{5,6},{5,8},
                {6,3},{6,4},{6,5},{6,6},
                {8,1},{8,4},{8,5},{8,8}
        };

        for (int[] pos : affectedTiles) {
            int row = pos[0];
            int col = pos[1];

            meteorTiles[row][col] = true;
            permanentFertilizer[row][col] = false;
            wateredTiles[row][col] = false;
            fertilizerDays[row][col] = 0;

            plantedName[row][col] = "";
            plantedYield[row][col] = 0;
            plantedPreferredSoil[row][col] = "";
            plantedGrowth[row][col] = 0;
        }

        refreshAllTiles();
    }

    private void handleTileExcavate(int row, int col) {
        if (!meteorTiles[row][col]) {
            showWarning("You can only excavate grey meteor tiles.");
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            showWarning("You have already used all 5 excavations for today.");
            excavateMode = false;
            updateExcavateButtonState();
            return;
        }

        if (savings < EXCAVATE_COST) {
            showWarning("You need 500 savings to excavate this tile.");
            excavateMode = false;
            return;
        }

        savings -= EXCAVATE_COST;
        savingsLabel.setText("Savings: " + savings);

        meteorTiles[row][col] = false;
        permanentFertilizer[row][col] = true;
        wateredTiles[row][col] = false;
        fertilizerDays[row][col] = 0;

        excavationsToday++;
        excavateMode = false;

        updateTileAppearance(row, col, tileButtons[row][col]);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Excavation Successful");
        alert.setContentText(
                "Tile excavated.\n" +
                        "Cost: " + EXCAVATE_COST + "\n" +
                        "This tile is now permanently fertilized.\n" +
                        "Remaining excavations today: " + (MAX_EXCAVATIONS_PER_DAY - excavationsToday)
        );
        alert.showAndWait();

        updateExcavateButtonState();
    }

    public boolean plantSelectedTile(String name, int price, int yield, int maxGrowth, String preferredSoil, int cropPrice) {
        if (selectedRow == -1 || selectedCol == -1) return false;
        if (meteorTiles[selectedRow][selectedCol]) return false;
        if (hasPlant(selectedRow, selectedCol)) return false;

        if (savings < price) {
            showWarning("You need " + price + " savings to buy " + name + ".");
            return false;
        }

        savings -= price;
        savingsLabel.setText("Savings: " + savings);

        plantedName[selectedRow][selectedCol] = name;
        plantedYield[selectedRow][selectedCol] = yield;
        plantedPreferredSoil[selectedRow][selectedCol] = preferredSoil;
        plantedGrowth[selectedRow][selectedCol] = 0;
        wateredTiles[selectedRow][selectedCol] = false;

        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);
        return true;
    }

    @FXML
    private void handlePlant() {
        if (selectedRow == -1 || selectedCol == -1) {
            showWarning("Click a tile first before planting.");
            return;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            showWarning("You cannot plant on a meteorite tile.");
            return;
        }

        if (hasPlant(selectedRow, selectedCol)) {
            showInfo("This tile already has a plant.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlantStore.fxml"));
            Scene scene = new Scene(loader.load(), 1500, 850);

            PlantStoreController controller = loader.getController();

            Stage storeStage = new Stage();
            controller.setGameController(this);
            controller.setStage(storeStage);

            storeStage.setTitle("Plant Store");
            storeStage.initModality(Modality.APPLICATION_MODAL);
            storeStage.setScene(scene);
            storeStage.showAndWait();

            refreshAllTiles();

        } catch (Exception e) {
            e.printStackTrace();
            showWarning("Could not open Plant Store.");
        }
    }

    @FXML
    private void handleWater() {
        if (selectedRow == -1 || selectedCol == -1) {
            showWarning("Click a tile first before watering.");
            return;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            showWarning("You cannot water a meteorite tile.");
            return;
        }

        if (!hasPlant(selectedRow, selectedCol)) {
            showWarning("There is no plant on this tile.");
            return;
        }

        if (wateredTiles[selectedRow][selectedCol]) {
            showInfo("This tile is already watered.");
            return;
        }

        if (water <= 0) {
            showWarning("Refill your watering can first.");
            return;
        }

        water--;
        wateredTiles[selectedRow][selectedCol] = true;

        waterLabel.setText("Water: " + water + "/" + maxWater);
        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);

        showInfo("Tile (" + (selectedCol + 1) + "," + (selectedRow + 1) + ") has been watered.");
    }

    @FXML
    private void handleRefillWater() {
        if (water == maxWater) {
            showInfo("Your watering can is already full.");
            return;
        }

        if (savings < REFILL_COST) {
            showWarning("You need 100 savings to refill the watering can.");
            return;
        }

        savings -= REFILL_COST;
        water = maxWater;

        savingsLabel.setText("Savings: " + savings);
        waterLabel.setText("Water: " + water + "/" + maxWater);

        showInfo("Your watering can has been refilled for 100.");
    }

    @FXML
    private void handleFertilizer() {
        if (selectedRow == -1 || selectedCol == -1) {
            showWarning("Click a tile first before applying fertilizer.");
            return;
        }

        if (meteorTiles[selectedRow][selectedCol]) {
            showWarning("You cannot fertilize a meteorite tile.");
            return;
        }

        if (permanentFertilizer[selectedRow][selectedCol]) {
            showInfo("This tile is already permanently fertilized.");
            return;
        }

        if (savings < FERTILIZER_COST) {
            showWarning("You need " + FERTILIZER_COST + " savings to apply fertilizer.");
            return;
        }

        savings -= FERTILIZER_COST;
        fertilizerDays[selectedRow][selectedCol] = FERTILIZER_DAYS;

        savingsLabel.setText("Savings: " + savings);
        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);

        showInfo("Fertilizer applied to tile (" + (selectedCol + 1) + "," + (selectedRow + 1) + ").");
    }

    @FXML
    private void handleHarvest() {
        if (selectedRow == -1 || selectedCol == -1) {
            showWarning("Click a tile first.");
            return;
        }

        if (!hasPlant(selectedRow, selectedCol)) {
            showWarning("There is no plant on this tile.");
            return;
        }

        char stage = getCurrentStageCode(selectedRow, selectedCol);
        String plant = plantedName[selectedRow][selectedCol];
        int yield = plantedYield[selectedRow][selectedCol];
        int earned = 0;

        if (stage == 'L') {
            earned = getLowStageSellPrice(plant) * yield;
            showInfo("Harvested " + plant + " for " + earned + " savings.");
        } else if (stage == 'H') {
            int pricePerPiece = getHighStageSellPrice(plant);
            if (isRootCrop(plant)) {
                pricePerPiece = (int) Math.round(pricePerPiece * 1.5);
            }
            earned = pricePerPiece * (yield * 2);
            showInfo("Harvested " + plant + " for " + earned + " savings.");
        } else if (stage == 'M') {
            int pricePerPiece = getHighStageSellPrice(plant);
            earned = pricePerPiece * (yield * 2);
            showInfo("Harvested " + plant + " for " + earned + " savings.");
        } else {
            showInfo("Plant removed. No crop was produced.");
        }

        savings += earned;
        savingsLabel.setText("Savings: " + savings);

        plantedName[selectedRow][selectedCol] = "";
        plantedYield[selectedRow][selectedCol] = 0;
        plantedPreferredSoil[selectedRow][selectedCol] = "";
        plantedGrowth[selectedRow][selectedCol] = 0;
        wateredTiles[selectedRow][selectedCol] = false;

        updateTileAppearance(selectedRow, selectedCol, tileButtons[selectedRow][selectedCol]);
    }

    private int getLowStageSellPrice(String plant) {
        return switch (plant) {
            case "Turnip" -> 5;
            case "Wheat" -> 4;
            case "Thyme" -> 0;
            case "Potato" -> 4;
            case "Tomato" -> 0;
            default -> 0;
        };
    }

    private int getHighStageSellPrice(String plant) {
        return switch (plant) {
            case "Turnip" -> 6;
            case "Wheat" -> 4;
            case "Thyme" -> 7;
            case "Potato" -> 8;
            case "Tomato" -> 5;
            default -> 0;
        };
    }

    private boolean isRootCrop(String plant) {
        return plant.equals("Turnip") || plant.equals("Potato");
    }

    private int computeGrowthSteps(int row, int col) {
        if (!hasPlant(row, col)) return 0;

        char stage = getCurrentStageCode(row, col);
        boolean watered = wateredTiles[row][col];
        boolean preferred = isPreferredSoil(row, col);
        boolean fertilized = hasActiveFertilizer(row, col);

        if (stage == 'M') {
            return 0;
        }

        if (stage == 'D') {
            return 1;
        }

        if (stage == 'E') {
            if (watered) return 0;
            return 1;
        }

        if (stage == 'S') {
            if (!watered) return 0;

            int steps = 1;
            if (preferred) steps += 2;
            if (fertilized) steps += 2;
            return steps;
        }

        if (stage == 'L' || stage == 'H') {
            if (!watered) return 0;

            int steps = 1;
            if (preferred) steps += 1;
            if (fertilized) steps += 1;
            return steps;
        }

        return 0;
    }

    private void advancePlantGrowth(int row, int col, int steps) {
        if (!hasPlant(row, col)) return;

        char[] stages = getPlantStages(plantedName[row][col]);
        int maxIndex = stages.length - 1;

        plantedGrowth[row][col] += steps;
        if (plantedGrowth[row][col] > maxIndex) {
            plantedGrowth[row][col] = maxIndex;
        }
    }

    private void processAllPlantGrowth() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (!hasPlant(r, c)) continue;

                int steps = computeGrowthSteps(r, c);
                advancePlantGrowth(r, c, steps);
            }
        }
    }

    private void reduceWaterFlags() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                wateredTiles[r][c] = false;
            }
        }
    }

    private void reduceFertilizerDays() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (permanentFertilizer[r][c]) continue;
                if (fertilizerDays[r][c] <= 0) continue;
                if (!hasPlant(r, c)) continue;

                char stage = getCurrentStageCode(r, c);

                if (stage == 'E') {
                    fertilizerDays[r][c] -= 2;
                } else {
                    fertilizerDays[r][c] -= 1;
                }

                if (fertilizerDays[r][c] < 0) {
                    fertilizerDays[r][c] = 0;
                }
            }
        }
    }

    private void endGameAndOpenLeaderboard() {
        if (scoreSaved) return;
        scoreSaved = true;

        try {
            String playerName = playerNameLabel.getText().trim();
            if (playerName.isEmpty()) playerName = "Unknown";

            LeaderboardManager.addEntry(playerName, savings);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Game Over");
            alert.setContentText("Final Savings: " + savings);
            alert.showAndWait();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Leaderboard.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            Stage stage = (Stage) farmGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Leaderboard");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNextDay() {
        if (currentDay >= 20) {
            endGameAndOpenLeaderboard();
            return;
        }

        processAllPlantGrowth();
        reduceWaterFlags();
        reduceFertilizerDays();

        savings += 50;
        savingsLabel.setText("Savings: " + savings);

        currentDay++;
        dayLabel.setText("Day " + currentDay);

        excavationsToday = 0;
        excavateMode = false;

        if (currentDay == 16 && !meteorTriggered) {
            meteorTriggered = true;
            triggerMeteoriteEvent();
            showInfo("A meteorite has struck the field!");
        }

        refreshAllTiles();
        updateExcavateButtonState();

        if (currentDay == 20) {
            showInfo("You have reached Day 20. Click Next Day again to end the game.");
        }
    }

    @FXML
    private void handleExcavate() {
        if (!meteorTriggered) {
            showInfo("Excavate is not available yet.");
            return;
        }

        if (excavationsToday >= MAX_EXCAVATIONS_PER_DAY) {
            showWarning("You have already used all 5 excavations for today.");
            updateExcavateButtonState();
            return;
        }

        int remaining = MAX_EXCAVATIONS_PER_DAY - excavationsToday;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Excavate Meteorite");
        alert.setContentText(
                "Cost per tile: " + EXCAVATE_COST + "\n" +
                        "Remaining excavations today: " + remaining + "\n\n" +
                        "Press OK, then click a grey tile to excavate."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            excavateMode = true;
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}