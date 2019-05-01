package ui.controller;

import engine.Army;
import engine.Player;
import engine.Territory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.MainApp;

import java.util.ArrayList;
import java.util.Optional;

public class AttackResultsDialogController implements SceneController {

    @FXML private HBox chooseAttackHBox;
    @FXML private VBox attackResultsVBox;
    @FXML private Button closeButton;
    @FXML private Label resultsDescriptionLabel;
    @FXML private TableView<Army> attackingArmiesTable;
    @FXML private TableColumn attackingArmyColumn;
    @FXML private TableColumn<Army, String> attackingArmyUnitTypeColumn;
    @FXML private TableColumn<Army, Number> attackingArmyAmountColumn;
    @FXML private TableColumn<Army, Number> attackingArmyFirepowerColumn;
    @FXML private TableView<Army> defendingArmiesTable;
    @FXML private TableColumn defendingArmyColumn;
    @FXML private TableColumn<Army, String> defendingArmyUnitTypeColumn;
    @FXML private TableColumn<Army, Number> defendingArmyAmountColumn;
    @FXML private TableColumn<Army, Number> defendingArmyFirepowerColumn;
    @FXML private TableView<Army> winningArmiesTable;
    @FXML private TableColumn winningArmyColumn;
    @FXML private TableColumn<Army, String> winningArmyUnitTypeColumn;
    @FXML private TableColumn<Army, Number> winningArmyAmountColumn;
    @FXML private TableColumn<Army, Number> winningArmyFirepowerColumn;

    private MainApp mainAppReference;
    private boolean attackPerformed = false;
    private Player attackingPlayer;
    private Player defendingPlayer;
    private ArrayList<Army> attackingArmies;
    private ArrayList<Army> defendingArmies;
    private Territory currentTerritory;
    private Stage dialogStage;
    private AttackType attackType;
    private ObservableList<Army> attackingArmiesData = FXCollections.observableArrayList();
    private ObservableList<Army> defendingArmiesData = FXCollections.observableArrayList();
    private ObservableList<Army> winningArmiesData = FXCollections.observableArrayList();

    private enum AttackType {
        IM_FEELING_LUCKY, DETERMINISTIC
    }

    public AttackResultsDialogController(MainApp mainApp){
        this.mainAppReference = mainApp;
    }

    public void initialize(){
        initArmyTableAndColumns(attackingArmiesTable, attackingArmiesData, attackingArmyUnitTypeColumn, attackingArmyAmountColumn, attackingArmyFirepowerColumn);
        initArmyTableAndColumns(defendingArmiesTable, defendingArmiesData, defendingArmyUnitTypeColumn, defendingArmyAmountColumn, defendingArmyFirepowerColumn);
        initArmyTableAndColumns(winningArmiesTable, winningArmiesData, winningArmyUnitTypeColumn, winningArmyAmountColumn, winningArmyFirepowerColumn);
    }

    private void initArmyTableAndColumns(TableView<Army> table, ObservableList<Army> data, TableColumn<Army, String> unitTypeColumn, TableColumn<Army, Number> amountColumn, TableColumn<Army, Number> firepowerColumn){
        unitTypeColumn.setCellValueFactory(cellData -> cellData.getValue().getUnit().getTypeProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().getAmountProperty());
        firepowerColumn.setCellValueFactory(cellData -> cellData.getValue().getCompetenceProperty());
        table.setItems(data);
        data.clear();
    }

    public void performAttackCalculations(){
        attackPerformed = true;
        Army.uniteSameTypeArmies(attackingArmies);
        ArrayList<Army> copyOfAttackingArmies = Army.cloneArmies(attackingArmies, attackingPlayer);
        ArrayList<Army> copyOfDefendingArmies = Army.cloneArmies(defendingArmies, defendingPlayer);
        attackingArmiesData.addAll(copyOfAttackingArmies);
        defendingArmiesData.addAll(copyOfDefendingArmies);

        attackingArmyColumn.setText("Attacking Army - " + attackingPlayer.getName());
        defendingArmyColumn.setText("Defending Army - " + defendingPlayer.getName());

        ArrayList<Army> copyOfWinningArmies;
        if(attackType == AttackType.IM_FEELING_LUCKY)
            copyOfWinningArmies = currentTerritory.attackImFeelingLucky(attackingArmies, defendingArmies);
         else  //attackType == AttackType.DETERMINISTIC
            copyOfWinningArmies = currentTerritory.attackDeterministic(attackingArmies, defendingArmies);

        winningArmiesData.addAll(copyOfWinningArmies);

        StringBuilder attackResultsText = new StringBuilder();
        if(copyOfWinningArmies.size() > 0){
            if(currentTerritory.getConqueringPlayer() != defendingPlayer) { // Attacking player won
                attackResultsText.append(attackingPlayer.getName()).append(" won the battle!\n");
                winningArmyColumn.setText("Winning Army - " + attackingPlayer.getName());

                if(currentTerritory.getConqueringPlayer() == attackingPlayer)
                    attackResultsText.append("Territory # ").append(currentTerritory.getId()).append(" is now under ").append(attackingPlayer.getName()).append("'s control.");
                else
                    attackResultsText.append("Territory # ").append(currentTerritory.getId()).append(" remains neutral.");

            } else {
                attackResultsText.append(attackingPlayer.getName()).append(" lost the battle...\n");
                winningArmyColumn.setText("Winning Army - " + defendingPlayer.getName());
            }
            resultsDescriptionLabel.setText(attackResultsText.toString());
        } else { // It's a tie
            resultsDescriptionLabel.setText("Both armies lost!");
            winningArmyColumn.setText("No Winning Army");
        }
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }

    @FXML
    private void handleClose(){
        dialogStage.close();
    }

    public boolean isAttackPerformed() { return attackPerformed; }

    public void setCurrentTerritory(Territory territory) {
        currentTerritory = territory;
        defendingPlayer = territory.getConqueringPlayer();
        defendingArmies = territory.getArmies();
    }

    public void setAttackingArmies(ArrayList<Army> army) { attackingArmies = army; }

    public void setAttackingPlayer(Player player){
        attackingPlayer = player;
    }

    public boolean showChooseAttackTypeAlert(){
        Alert chooseAttackAlert = new Alert(Alert.AlertType.CONFIRMATION);
        chooseAttackAlert.setTitle("Choose Attack Type");
        chooseAttackAlert.setHeaderText("Choose Attack Type:");

        ButtonType imFeelingLuckyButton = new ButtonType("I'm Feeling Lucky");
        ButtonType deterministicButton = new ButtonType("Deterministic");
        ButtonType cancelButton = new ButtonType("Cancel");

        // Remove default ButtonTypes
        chooseAttackAlert.getButtonTypes().clear();

        chooseAttackAlert.getButtonTypes().addAll(imFeelingLuckyButton, deterministicButton, cancelButton);

        Optional<ButtonType> clickedButton = chooseAttackAlert.showAndWait();

        if(clickedButton.isPresent()) {
            if (clickedButton.get() == imFeelingLuckyButton) {
                attackType = AttackType.IM_FEELING_LUCKY;
                return true;
            } else if (clickedButton.get() == deterministicButton) {
                attackType = AttackType.DETERMINISTIC;
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }
}
