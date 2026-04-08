package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class LeaderboardController {

    @FXML private Label firstName;
    @FXML private Label firstSavings;
    @FXML private Label secondName;
    @FXML private Label secondSavings;
    @FXML private Label thirdName;
    @FXML private Label thirdSavings;

    @FXML
    public void initialize() {
        List<LeaderboardEntry> list = LeaderboardManager.loadLeaderboard();

        if (list.size() > 0) {
            firstName.setText("1. " + list.get(0).getName());
            firstSavings.setText(String.valueOf(list.get(0).getSavings()));
        } else {
            firstName.setText("1. ---");
            firstSavings.setText("0");
        }

        if (list.size() > 1) {
            secondName.setText("2. " + list.get(1).getName());
            secondSavings.setText(String.valueOf(list.get(1).getSavings()));
        } else {
            secondName.setText("2. ---");
            secondSavings.setText("0");
        }

        if (list.size() > 2) {
            thirdName.setText("3. " + list.get(2).getName());
            thirdSavings.setText(String.valueOf(list.get(2).getSavings()));
        } else {
            thirdName.setText("3. ---");
            thirdSavings.setText("0");
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StartMenu.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}