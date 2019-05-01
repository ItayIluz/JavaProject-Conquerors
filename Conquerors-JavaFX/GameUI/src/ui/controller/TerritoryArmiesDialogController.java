package ui.controller;

import engine.Army;
import engine.Player;
import engine.Territory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ui.MainApp;

import java.util.ArrayList;

public class TerritoryArmiesDialogController implements SceneController {

    @FXML private TableView<Army> territoryArmiesTable;
    @FXML private TableColumn<Army, String> territoryArmiesUnitTypeColumn;
    @FXML private TableColumn<Army, Number> territoryArmiesAmountColumn;
    @FXML private TableColumn<Army, Number> territoryArmiesCompetenceColumn;
    @FXML private TableColumn<Army, Number> territoryArmiesCostToFixColumn;
    @FXML private Button confirmButton;
    @FXML private Button addArmyButton;
    @FXML private Button closeButton;
    @FXML private Button removeNewArmyButton;
    @FXML private Button fixCompetenceButton;
    @FXML private Label totalFirepowerLabel;
    @FXML private Label totalNewCostLabel;
    @FXML private Label totalNewCostText;
    @FXML private Label totalFixCostLabel;
    @FXML private Label totalFixCostText;

    public enum DialogModes{
        VIEW_ARMIES, MAINTAIN, CONQUER, ATTACK
    }

    private MainApp mainAppReference;
    private boolean actionConfirmed = false;
    private Player currentPlayer;
    private Territory currentTerritory;
    private Stage dialogStage;
    private Army selectedArmy;
    private DialogModes mode;
    private int totalNewArmiesCost = 0;
    private int totalArmiesFirepower = 0;
    private int totalCostToFixCompetence = 0;
    private int numOfNewArmies = 0;
    private AddArmyDialogController addArmyDialogController;
    private AttackResultsDialogController attackResultsDialogController;
    private ObservableList<Army> territoryArmiesData = FXCollections.observableArrayList();

    public TerritoryArmiesDialogController(MainApp mainApp){
        this.mainAppReference = mainApp;
    }

    public void initialize(){
        territoryArmiesUnitTypeColumn.setCellValueFactory(cellData -> cellData.getValue().getUnit().getTypeProperty());
        territoryArmiesAmountColumn.setCellValueFactory(cellData -> cellData.getValue().getAmountProperty());
        territoryArmiesCompetenceColumn.setCellValueFactory(cellData -> cellData.getValue().getCompetenceProperty());
        territoryArmiesCostToFixColumn.setCellValueFactory(cellData -> cellData.getValue().getCompetenceFixCostProperty());

        territoryArmiesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(newValue.isNew()){
                        selectedArmy = newValue;
                        removeNewArmyButton.setDisable(false);
                    } else {
                        removeNewArmyButton.setDisable(true);
                    }
                });

        territoryArmiesTable.setItems(territoryArmiesData);
        territoryArmiesData.clear();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleClose(){
        dialogStage.close();
    }

    @FXML
    private void handleAddArmy(){
        if(openAddArmyDialog()){
            Army newArmy = addArmyDialogController.getCreatedArmy();

            if(newArmy != null){
                totalNewArmiesCost += addArmyDialogController.getCreatedArmyCost();
                totalNewCostLabel.setText(totalNewArmiesCost + "");
                totalArmiesFirepower += addArmyDialogController.getCreatedArmyFirepower();
                totalFirepowerLabel.setText(totalArmiesFirepower + "");
                newArmy.setInTerritory(currentTerritory);
                newArmy.setNew(true);
                territoryArmiesData.add(newArmy);
                numOfNewArmies++;
                confirmButton.setDisable(false);
            }
        }
    }

    @FXML
    private void handleFixCompetence() {
        ArrayList<Army> currentTerritoryArmies = currentTerritory.getArmies();

        if (currentPlayer.hasEnoughMoney(totalCostToFixCompetence)) {
            for (Army currentArmy : currentTerritoryArmies)
                currentArmy.fixCompetence();
            Army.uniteSameTypeArmies(currentTerritoryArmies);
            currentPlayer.subtractMoney(totalCostToFixCompetence);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(dialogStage);
            alert.setTitle("Competence Fixed!");
            alert.setHeaderText("Armies' competence fixed!");

            alert.showAndWait();
            actionConfirmed = true;
            dialogStage.close();
        } else {
            mainAppReference.popupAlert(dialogStage, "You don't have enough Turings!", Alert.AlertType.ERROR);
        }
    }

    private boolean openAddArmyDialog(){
        addArmyDialogController = new AddArmyDialogController(mainAppReference);
        Stage addArmyStage = mainAppReference.loadNewDialogScene("view/AddArmyDialog.fxml", addArmyDialogController, "Add Army", dialogStage);

        addArmyDialogController.setDialogStage(addArmyStage);
        addArmyDialogController.setCurrentPlayer(currentPlayer);

        addArmyStage.showAndWait();

        return addArmyDialogController.isConfirmClicked();
    }

    private boolean openAttackResultsDialog(ArrayList<Army> attackingArmy){
        attackResultsDialogController = new AttackResultsDialogController(mainAppReference);
        Stage attackResultsStage = mainAppReference.loadNewDialogScene("view/AttackResultsDialog.fxml", attackResultsDialogController, "Attack Results", dialogStage);

        attackResultsDialogController.setDialogStage(attackResultsStage);
        attackResultsDialogController.setCurrentTerritory(currentTerritory);
        attackResultsDialogController.setAttackingPlayer(currentPlayer);
        attackResultsDialogController.setAttackingArmies(attackingArmy);

        if(attackResultsDialogController.showChooseAttackTypeAlert()) {
            attackResultsDialogController.performAttackCalculations();
            attackResultsStage.showAndWait();
        }

        return attackResultsDialogController.isAttackPerformed();
    }

    @FXML
    private void handleConfirm(){
        String errorMessage = null;
        if(numOfNewArmies > 0) {
            int totalArmyFirepower = 0;
            int territoryThreshold = currentTerritory.getArmyThreshold();
            ArrayList<Army> territoryArmies = new ArrayList<>(territoryArmiesData);
            for (Army army : territoryArmies)
                totalArmyFirepower += army.getCompetence();

            if (totalArmyFirepower < territoryThreshold) // Didn't pass threshold
                errorMessage = "You must add units with at least " + territoryThreshold + " firepower to conquer this territory!";

            if(!currentPlayer.hasEnoughMoney(totalNewArmiesCost))
                errorMessage = "You don't have enough Turings!";

            if(errorMessage != null){
                mainAppReference.popupAlert(dialogStage,errorMessage, Alert.AlertType.ERROR);
            } else {
                if(mode == DialogModes.ATTACK){
                    if(openAttackResultsDialog(territoryArmies)){
                        currentPlayer.subtractMoney(totalNewArmiesCost);
                        actionConfirmed = true;
                        dialogStage.close();
                    }
                } else {
                    for (Army army : territoryArmies)
                        army.setNew(false);

                    Army.uniteSameTypeArmies(territoryArmies);
                    currentTerritory.setConqueredByPlayer(currentPlayer, territoryArmies);
                    for(Army army : territoryArmies)
                        army.getUnit().addToTotalOnBoard(army.getAmount());
                    currentPlayer.subtractMoney(totalNewArmiesCost);
                    actionConfirmed = true;
                    dialogStage.close();
                }
            }
        } else {
            actionConfirmed = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleRemove(){
        if(selectedArmy != null){
            if(selectedArmy.isNew()) {
                totalNewArmiesCost -= selectedArmy.getTotalCost();
                totalArmiesFirepower -= selectedArmy.getCompetence();
                totalNewCostLabel.setText("" + totalNewArmiesCost);
                totalFirepowerLabel.setText("" + totalArmiesFirepower);
                territoryArmiesData.remove(selectedArmy);
                numOfNewArmies--;
            }
        }

        if(numOfNewArmies == 0) {
            confirmButton.setDisable(true);
            selectedArmy = null;
        }
    }

    private void showAddArmyButtonsAndLabels(boolean showButtons, boolean showLabels){
        showNodeVisibleAndManages(confirmButton, showButtons);
        showNodeVisibleAndManages(addArmyButton, showButtons);
        showNodeVisibleAndManages(removeNewArmyButton, showButtons);
        showNodeVisibleAndManages(totalNewCostText, showLabels);
        showNodeVisibleAndManages(totalNewCostLabel, showLabels);
    }

    private void showFixCompetenceButtonsLabelsColumn(boolean showButtons, boolean showLabelsAndColumn){
        showNodeVisibleAndManages(fixCompetenceButton, showButtons);
        showNodeVisibleAndManages(totalFixCostLabel, showLabelsAndColumn);
        showNodeVisibleAndManages(totalFixCostText, showLabelsAndColumn);
        territoryArmiesCostToFixColumn.setVisible(showLabelsAndColumn);
    }

    public void setMode(DialogModes mode){
        this.mode = mode;
        confirmButton.setDisable(true);
        removeNewArmyButton.setDisable(true);
        switch(mode){
            case VIEW_ARMIES: {

                for (Army currentArmy : currentTerritory.getArmies())
                    totalCostToFixCompetence += currentArmy.calculateCompetenceFixCost();

                territoryArmiesData.addAll(currentTerritory.getArmies());
                totalArmiesFirepower = currentTerritory.getTotalFirepower();
                showAddArmyButtonsAndLabels(false, false);
                showFixCompetenceButtonsLabelsColumn(false, true);


                break;

            } case MAINTAIN: {

                for (Army currentArmy : currentTerritory.getArmies())
                    totalCostToFixCompetence += currentArmy.calculateCompetenceFixCost();

                territoryArmiesData.addAll(currentTerritory.getArmies());
                totalArmiesFirepower = currentTerritory.getTotalFirepower();
                showAddArmyButtonsAndLabels(true, true);
                showFixCompetenceButtonsLabelsColumn(true, true);

                break;

            } case CONQUER: {

                showAddArmyButtonsAndLabels(true, true);
                showFixCompetenceButtonsLabelsColumn(false, false);

                break;

            } case ATTACK: {

                showFixCompetenceButtonsLabelsColumn(false, false);

                break;
            }
        }
        totalFixCostLabel.setText("" + totalCostToFixCompetence);
        totalFirepowerLabel.setText("" + totalArmiesFirepower);
    }

    private void showNodeVisibleAndManages(Node node, boolean show){
        node.setVisible(show);
        node.setManaged(show);
    }

    public boolean isActionConfirmed(){
        return actionConfirmed;
    }

    public void setCurrentPlayer(Player player){
        currentPlayer = player;
    }

    public void setCurrentTerritory(Territory territory) {
        currentTerritory = territory;
    }
}
