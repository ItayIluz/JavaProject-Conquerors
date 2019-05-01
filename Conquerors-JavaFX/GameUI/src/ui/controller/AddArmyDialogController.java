package ui.controller;

import engine.Army;
import engine.GameData;
import engine.Player;
import engine.Unit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ui.MainApp;

import java.util.Comparator;
import java.util.Map;

public class AddArmyDialogController implements SceneController {

    @FXML private ComboBox<Unit> unitTypeComboBox;
    @FXML private TextField amountTextField;
    @FXML private Label totalCostLabel;
    @FXML private Label notEnoughLabel;
    @FXML private Label totalFirepowerLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private MainApp mainAppReference;
    private boolean confirmClicked = false;
    private Player currentPlayer;
    private Stage dialogStage;
    private Army createdArmy;
    private int createdArmyCost = 0;
    private int createdArmyFirepower = 0;

    public AddArmyDialogController(MainApp mainApp){
        this.mainAppReference = mainApp;
    }

    public void initialize(){
        notEnoughLabel.setManaged(false);

        GameData gameData = mainAppReference.getGameData();

        for(Unit currentUnit : gameData.getGameUnits().values())
            unitTypeComboBox.getItems().add(currentUnit);

        unitTypeComboBox.setConverter(new StringConverter<Unit>() {

            @Override
            public String toString(Unit unit) {
                return gameData.capitalizeString(unit.getType()) + ", Cost " + unit.getPurchasePrice() + ", Rank " + unit.getRank() + ", Firepower " + unit.getMaxFirePower();
            }

            @Override
            public Unit fromString(String string) {
                return unitTypeComboBox.getItems().stream().filter(unit ->
                        unit.getType().equals(string)).findFirst().orElse(null);
            }
        });

        unitTypeComboBox.getItems().sort(Comparator.comparingInt(Unit::getRank));

        unitTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> handleInputChange());

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                amountTextField.setText(newValue.replaceAll("[^\\d]", ""));
            else
                handleInputChange();
        });

        amountTextField.setText("");
        totalCostLabel.setText("0 Turings");
        totalFirepowerLabel.setText("0");
        createdArmy = null;
        notEnoughLabel.setManaged(false);
        notEnoughLabel.setVisible(false);
        confirmButton.setDisable(true);
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    private void handleInputChange(){
        Unit selectedUnit = unitTypeComboBox.getValue();
        String textFieldValue = amountTextField.getText();

        if(selectedUnit != null && !textFieldValue.isEmpty()){

            int amountToAdd = Integer.parseInt(textFieldValue);
            if (amountToAdd > 0) { // Must add more than 0
                int totalCost = selectedUnit.getPurchasePrice() * amountToAdd;
                int totalFirepower = selectedUnit.getMaxFirePower() * amountToAdd;
                totalCostLabel.setText(totalCost + " Turings");
                totalFirepowerLabel.setText(totalFirepower + "");

                if (!currentPlayer.hasEnoughMoney(totalCost)) { // If the player doesn't have enough money
                    notEnoughLabel.setManaged(true);
                    notEnoughLabel.setVisible(true);
                    confirmButton.setDisable(true);
                } else {
                    notEnoughLabel.setManaged(false);
                    notEnoughLabel.setVisible(false);
                    confirmButton.setDisable(false);
                    createdArmy = new Army(amountToAdd, selectedUnit, currentPlayer);
                    createdArmyCost = totalCost;
                    createdArmyFirepower = createdArmy.getCompetence();
                }
            }
        }
    }

    @FXML
    private void handleConfirm(){
        confirmClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }

    public boolean isConfirmClicked() { return  confirmClicked; }

    public Army getCreatedArmy() { return createdArmy; }

    public int getCreatedArmyCost() { return createdArmyCost; }

    public int getCreatedArmyFirepower() { return createdArmyFirepower; }

    public void setCurrentPlayer(Player player){
        currentPlayer = player;
    }
}
