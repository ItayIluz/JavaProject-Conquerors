package ui.controller;

import engine.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ui.MainApp;

import java.io.File;
import java.util.ArrayList;

public class GameSceneController implements SceneController {

    @FXML private TableView<Player> playerTable;
    @FXML private TableColumn<Player, String> playerNameColumn;
    @FXML private TableColumn<Player, Number> playerIdColumn;
    @FXML private TableColumn<Player, String> playerColorColumn;
    @FXML private TableColumn<Player, Number> playerMoneyColumn;
    @FXML private TableView<Unit> unitsTable;
    @FXML private TableColumn<Unit, String> unitTypeColumn;
    @FXML private TableColumn<Unit, Number> unitRankColumn;
    @FXML private TableColumn<Unit, Number> unitCostColumn;
    @FXML private TableColumn<Unit, Number> unitMaxFirepowerColumn;
    @FXML private TableColumn<Unit, Number> unitCompetenceReductionColumn;
    @FXML private TableColumn<Unit, Number> unitSingleFirepowerCostColumn;
    @FXML private TableColumn<Unit, Number> unitTotalUnitsOnBoardColumn;
    @FXML private AnchorPane boardAnchorPane;
    @FXML private Button startGameButton;
    @FXML private Button startNextRoundButton;
    @FXML private Button undoLastRoundButton;
    @FXML private Button saveGameButton;
    @FXML private Button backToMenuButton;
    @FXML private Button endTurnButton;
    @FXML private Button takeActionButton;
    @FXML private Button surrenderButton;
    @FXML private Button forceEndGameButton;
    @FXML private Label currentPlayerText;
    @FXML private Label currentPlayerLabel;
    @FXML private Label currentRoundText;
    @FXML private Label currentRoundLabel;
    @FXML private Label replayModeLabel;
    @FXML private Button replayModeNextButton;
    @FXML private Button replayModePreviousButton;
    @FXML private CheckBox showAnimationsCheckbox;
    @FXML private ComboBox<String> changeThemeComboBox;

    private MainApp mainApp; // Reference to the main application.
    private GameData gameData;
    private GridPane boardGrid;
    private Player currentPlayer = null;
    private int replayRound;
    private int numberOfSurrenderedPlayers = 0;
    private Button[] territoryActionButtons;
    private Button[] territoryArmiesButtons;
    private ObservableList<Unit> unitsData = FXCollections.observableArrayList();
    private ObservableList<Player> playersData = FXCollections.observableArrayList();

    private enum ActionOnTerritory{
        CONQUER, ATTACK, MAINTAIN
    }

    public GameSceneController(MainApp mainApp) {
        this.mainApp = mainApp;
        gameData = mainApp.getGameData();
    }

    @FXML
    private void initialize() {

        Territory[] territories = gameData.getBoard().getTerritories();
        showReplayModeButtons(false);
        initGameTables();
        initGameBoard(territories);
        playerTable.refresh();

        currentRoundText.setVisible(true);
        currentRoundText.setManaged(true);
        currentRoundLabel.setVisible(true);
        currentRoundLabel.setManaged(true);
        currentRoundLabel.setText(gameData.getCurrentRound() + " / " + gameData.getTotalRounds());
        disableNewRoundButtons(true);

        changeThemeComboBox.getSelectionModel().select(mainApp.getCurrentTheme());
        changeThemeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mainApp.setCurrentTheme(newValue);
            playerTable.refresh();
        });

        if(gameData.didGameStart()){ // Then game was loaded from a save game file
            for(Territory territory : territories)
                colorTerritoryByConqueror(territory);

            startGameButton.setDisable(true);
            startNextRoundButton.setDisable(false);
            undoLastRoundButton.setDisable(false);
            saveGameButton.setDisable(false);
        }
    }

    @FXML
    private void handleStartGame(){
        if(!gameData.didGameStartFirstTime()) {
            gameData.startGame(true);
            gameData.setGameStartedFirstTime(true);
        } else {
            showReplayModeButtons(false);
            gameData.startGame(false);

            for(Territory territory : gameData.getBoard().getTerritories())
                colorTerritoryByConqueror(territory);

            initGameTables();
            initGameBoard(gameData.getBoard().getTerritories());
            for(Unit unit : gameData.getGameUnits().values())
                unit.setTotalOnBoard(0);
        }

        numberOfSurrenderedPlayers = 0;
        startGameButton.setDisable(true);
        startNextRoundButton.setDisable(false);
        saveGameButton.setDisable(false);
        disableNewRoundButtons(true);
        currentRoundText.setVisible(true);
        currentRoundText.setManaged(true);
        currentRoundLabel.setVisible(true);
        currentRoundLabel.setManaged(true);
        currentRoundLabel.setText("0 / " + gameData.getTotalRounds());
    }

    private void disableNewRoundButtons(boolean disable){
        endTurnButton.setDisable(disable);
        takeActionButton.setDisable(disable);
        surrenderButton.setDisable(disable);
        forceEndGameButton.setDisable(disable);
        currentPlayerText.setVisible(!disable);
        currentPlayerText.setManaged(!disable);
        currentPlayerLabel.setVisible(!disable);
        currentPlayerLabel.setManaged(!disable);
    }

    private Background createBackgroundColorByPlayer(Player player, double animationFrac){

        Color backgroundColor;
        if(player.isSurrendered())
            backgroundColor = new Color(0,0,0, 0 + animationFrac);
        else {
            Color playerColor = player.getColor();
            backgroundColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 0 + animationFrac);
        }

        return new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY));
    }

    private void colorTerritoryByConqueror(Territory territory){
        VBox territoryVBox = (VBox) boardGrid.getChildren().get(territory.getId());

        if(showAnimationsCheckbox.isSelected()) {
            final Animation animation = new Transition() {

                {
                    setCycleDuration(Duration.millis(750));
                    setInterpolator(Interpolator.EASE_BOTH);
                }

                @Override
                protected void interpolate(double animationFrac) {
                    Background coloredBackground = null;
                    Player conqueringPlayer = territory.getConqueringPlayer();

                    if (conqueringPlayer != null)
                        coloredBackground = createBackgroundColorByPlayer(conqueringPlayer, animationFrac);
                    else {
                        Background territoryBackground = territoryVBox.getBackground();
                        if (territoryBackground != null) {
                            Color territoryColor = (Color) territoryBackground.getFills().get(0).getFill();
                            Color backgroundColor = new Color(territoryColor.getRed(), territoryColor.getGreen(), territoryColor.getBlue(), 1 - animationFrac);
                            coloredBackground = new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY));
                        }
                    }

                    territoryVBox.setBackground(coloredBackground);
                }
            };
            animation.play();
        } else {
            Background coloredBackground;
            Player conqueringPlayer = territory.getConqueringPlayer();

            if (conqueringPlayer != null)
                coloredBackground = createBackgroundColorByPlayer(conqueringPlayer, 1);
            else
                coloredBackground = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

            territoryVBox.setBackground(coloredBackground);
        }
    }

    private void setNextPlayer() {
        currentPlayer = gameData.setNextPlayer();
        showOnlyPlayerTerritoryArmiesButton();

        while(currentPlayer != null && currentPlayer.isSurrendered())
            currentPlayer = gameData.setNextPlayer();

        if(currentPlayer != null) {
            ArrayList<Territory> lostTerritories = currentPlayer.gainProfitsAndUpdateArmyCompetence();
            for(Territory territory : lostTerritories)
                colorTerritoryByConqueror(territory);

            takeActionButton.setDisable(false);
            currentPlayerLabel.setText(currentPlayer.getNameAndId());

        } else { // End round
            startNextRoundButton.setDisable(false);
            disableNewRoundButtons(true);

            if(gameData.isGameOver())
                calculateAndShowGameOverDialog();
            else {
                gameData.addHistory(gameData.getBoard().getTerritories());
                undoLastRoundButton.setDisable(false);
                saveGameButton.setDisable(false);
            }
        }

        playerTable.refresh();
    }

    private void calculateAndShowGameOverDialog(){
        currentPlayer = null;
        showOnlyPlayerTerritoryArmiesButton();
        startGameButton.setDisable(false);
        startNextRoundButton.setDisable(true);
        saveGameButton.setDisable(true);
        undoLastRoundButton.setDisable(true);
        disableNewRoundButtons(true);
        playerTable.refresh();

        GameOverDialogController controller = new GameOverDialogController(mainApp);
        Stage dialogStage = mainApp.loadNewDialogScene("view/GameOverDialog.fxml", controller, "Game Over!", mainApp.getPrimaryStage());

        controller.setDialogStage(dialogStage);
        controller.setPlayers(gameData.getPlayers());
        controller.setBoardGrid(boardGrid);
        controller.setShowAnimations(showAnimationsCheckbox.isSelected());
        controller.setWinningPlayers(gameData.endgameAftermath());

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

        if(controller.isReplayMode()) {
            showReplayModeButtons(true);
            replayRound = gameData.getCurrentRound();
            replayModeNextButton.setDisable(true);
        }
    }

    @FXML
    private void handleReplayPrevious(){
        replayRound--;
        gameData.setReplayData(replayRound);
        renderHistoryChange();
        replayModeNextButton.setDisable(false);
        currentRoundLabel.setText(replayRound + " / " + gameData.getTotalRounds());
        if(replayRound == 0)
            replayModePreviousButton.setDisable(true);
    }

    @FXML
    private void handleReplayNext(){
        replayRound++;
        gameData.setReplayData(replayRound);
        renderHistoryChange();
        replayModePreviousButton.setDisable(false);
        currentRoundLabel.setText(replayRound + " / " + gameData.getTotalRounds());
        if(replayRound == gameData.getCurrentRound())
            replayModeNextButton.setDisable(true);
    }

    private void showReplayModeButtons(boolean show){
        replayModeLabel.setManaged(show);
        replayModeLabel.setVisible(show);
        replayModeNextButton.setManaged(show);
        replayModeNextButton.setVisible(show);
        replayModePreviousButton.setManaged(show);
        replayModePreviousButton.setVisible(show);
        endTurnButton.setManaged(!show);
        endTurnButton.setVisible(!show);
        takeActionButton.setManaged(!show);
        takeActionButton.setVisible(!show);
        surrenderButton.setManaged(!show);
        surrenderButton.setVisible(!show);
        forceEndGameButton.setManaged(!show);
        forceEndGameButton.setVisible(!show);
    }

    @FXML
    private void handleNextRound(){
        startNextRoundButton.setDisable(true);
        undoLastRoundButton.setDisable(true);
        saveGameButton.setDisable(true);
        disableNewRoundButtons(false);
        setNextPlayer();
        currentRoundLabel.setText(gameData.incrementCurrentRound() + " / " + gameData.getTotalRounds());
    }

    @FXML
    private void handleUndoLastRound(){
        gameData.undoRound();

        renderHistoryChange();
        currentRoundLabel.setText(gameData.getCurrentRound() + " / " + gameData.getTotalRounds());

        if(gameData.getCurrentRound() == 0)
            undoLastRoundButton.setDisable(true);
    }

    private void renderHistoryChange(){
        initGameTables();
        initGameBoard(gameData.getBoard().getTerritories());
        playerTable.refresh();
        for(Territory territory : gameData.getBoard().getTerritories())
            colorTerritoryByConqueror(territory);
    }

    @FXML
    private void handleSaveGame(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "DAT Files (*.dat)", "*.dat");
        fileChooser.getExtensionFilters().add(extFilter);
        File fileToSave = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if(fileToSave != null){
            String saveGameErrors = gameData.saveGame(fileToSave);

            if (saveGameErrors != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error!");
                alert.setHeaderText("Game saving failed due to the following errors:");
                alert.setContentText(saveGameErrors);

                alert.show();
            } else {
                mainApp.popupAlert(mainApp.getPrimaryStage(), "Game Saved Successfully!", Alert.AlertType.INFORMATION);
            }
        }
    }

    @FXML
    private void handleBackToMenu(){
        mainApp.toggleMenuGameScenes();
    }

    @FXML
    private void handleEndTurn(){
        setNextPlayer();
    }

    @FXML
    private void handleTakeAction(){
        showActionTerritoryButtons();
    }

    @FXML
    private void handleForceEndGame(){
        gameData.addHistory(gameData.getBoard().getTerritories());
        calculateAndShowGameOverDialog();
    }

    @FXML
    private void handleSurrender(){
        numberOfSurrenderedPlayers++;
        for(Territory territory : currentPlayer.setSurrendered(true))
            colorTerritoryByConqueror(territory);

        playerTable.refresh();

        if(numberOfSurrenderedPlayers == gameData.getPlayers().length-1){
            gameData.addHistory(gameData.getBoard().getTerritories());
            calculateAndShowGameOverDialog();
        } else
            setNextPlayer();
    }

    private void showOnlyPlayerTerritoryArmiesButton(){
        Board board = gameData.getBoard();
        Territory[] boardTerritories = board.getTerritories();

        for(int i = 0; i < boardTerritories.length; i++) {
            final Territory currentTerritory = boardTerritories[i];
            setActionButtonState(territoryActionButtons[i], false, null, null);
            if(currentPlayer != null && currentTerritory.getConqueringPlayer() == currentPlayer) {
                territoryArmiesButtons[i].setVisible(true);
                territoryArmiesButtons[i].setManaged(true);
                territoryArmiesButtons[i].setOnAction(event -> openTerritoryArmiesDialog(currentTerritory, TerritoryArmiesDialogController.DialogModes.VIEW_ARMIES));
            } else {
                territoryArmiesButtons[i].setVisible(false);
                territoryArmiesButtons[i].setManaged(false);
            }
        }
    }

    private void showActionTerritoryButtons() {
        Board board = gameData.getBoard();
        Territory[] boardTerritories = board.getTerritories();
        int numberOfPlayerOwnedTerritories = currentPlayer.getOwnedTerritories().size();

        for(int i = 0; i < boardTerritories.length; i++) {
            final Territory currentTerritory = boardTerritories[i];
            territoryArmiesButtons[i].setVisible(false);
            territoryArmiesButtons[i].setManaged(false);
            if(currentTerritory.getConqueringPlayer() == currentPlayer){
                setActionButtonState(territoryActionButtons[i], true, ActionOnTerritory.MAINTAIN,
                        event -> openTerritoryArmiesDialog(currentTerritory, TerritoryArmiesDialogController.DialogModes.MAINTAIN));

            } else if(board.isTerritoryInPlayerRange(i, currentPlayer) || numberOfPlayerOwnedTerritories == 0){
                if(boardTerritories[i].isConquered()) { // Selected territory is an opponent's territory
                    setActionButtonState(territoryActionButtons[i], true, ActionOnTerritory.ATTACK,
                            event -> openTerritoryArmiesDialog(currentTerritory, TerritoryArmiesDialogController.DialogModes.ATTACK));
                } else {
                    setActionButtonState(territoryActionButtons[i], true, ActionOnTerritory.CONQUER,
                        event -> openTerritoryArmiesDialog(currentTerritory, TerritoryArmiesDialogController.DialogModes.CONQUER));
                }
            } else {
                setActionButtonState(territoryActionButtons[i], false, null, null);
            }
        }
    }

    private void openTerritoryArmiesDialog(Territory territory, TerritoryArmiesDialogController.DialogModes mode){

        TerritoryArmiesDialogController controller = new TerritoryArmiesDialogController(mainApp);
        Stage dialogStage = mainApp.loadNewDialogScene("view/TerritoryArmiesDialog.fxml", controller, "Territory Armies", mainApp.getPrimaryStage());

        controller.setDialogStage(dialogStage);
        controller.setCurrentPlayer(currentPlayer);
        controller.setCurrentTerritory(territory);
        controller.setMode(mode);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

        if(controller.isActionConfirmed()){
            colorTerritoryByConqueror(territory);
            showOnlyPlayerTerritoryArmiesButton();
            takeActionButton.setDisable(true);
        }
    }

    private void setActionButtonState(Button button, boolean enabled, ActionOnTerritory action, EventHandler<ActionEvent> onActionHandler){
        button.setVisible(enabled);
        button.setManaged(enabled);

        if(enabled) {
            button.setText(gameData.capitalizeString(action.name()));
            button.setOnAction(onActionHandler);
        }
    }

    private void initGameTables(){

        playerNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        playerIdColumn.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
        playerColorColumn.setCellValueFactory(cellData -> cellData.getValue().getColorNameProperty());
        playerMoneyColumn.setCellValueFactory(cellData -> cellData.getValue().getMoneyProperty());

        playersData.clear();
        playersData.addAll(gameData.getPlayers());
        playerTable.setItems(playersData);

        playerColorColumn.setCellFactory(column -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                final TableCell thisCell = this;

                if(item != null) {
                    setText(item);

                    if(showAnimationsCheckbox.isSelected()) {
                        final Animation animation = new Transition() {

                            {
                                setCycleDuration(Duration.millis(500));
                                setInterpolator(Interpolator.EASE_OUT);
                            }

                            @Override
                            protected void interpolate(double animationFrac) {
                                renderTableCell(thisCell, animationFrac);
                            }
                        };
                        animation.play();
                    } else {
                        renderTableCell(thisCell, 1);
                    }
                }
            }
        });
        
        unitTypeColumn.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
        unitRankColumn.setCellValueFactory(cellData -> cellData.getValue().getRankProperty());
        unitCostColumn.setCellValueFactory(cellData -> cellData.getValue().getPurchasePriceProperty());
        unitMaxFirepowerColumn.setCellValueFactory(cellData -> cellData.getValue().getMaxFirePowerProperty());
        unitCompetenceReductionColumn.setCellValueFactory(cellData -> cellData.getValue().getCompetenceReductionProperty());
        unitSingleFirepowerCostColumn.setCellValueFactory(cellData -> cellData.getValue().getSingleFirePowerPriceProperty());
        unitTotalUnitsOnBoardColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalOnBoardProperty());

        unitsData.clear();
        unitsData.addAll(gameData.getGameUnits().values());
        unitsTable.setItems(unitsData);
        unitsTable.getSortOrder().add(unitRankColumn);
        unitsTable.sort();
    }

    private void renderTableCell(TableCell cell, double animationFrac){

        TableRow currentRow = cell.getTableRow();
        Player rowPlayer = (Player) currentRow.getItem();

        // Fix for CSS table-row-cell class not rendering with other themes
        currentRow.getStyleClass().remove("table-row-cell");

        if (!cell.isEmpty() && rowPlayer != null) {
            cell.setBackground(createBackgroundColorByPlayer(rowPlayer, 1));

            if(rowPlayer.isSurrendered())
                currentRow.setBackground(createBackgroundColorByPlayer(rowPlayer, 1));
            else if(currentPlayer == rowPlayer)
                currentRow.setBackground(createBackgroundColorByPlayer(currentPlayer, animationFrac));
        }
    }

    private void initGameBoard(Territory[] boardTerritories) {

        int boardRows = gameData.getBoard().getRows();
        int boardColumns = gameData.getBoard().getColumns();
        territoryActionButtons = new Button[boardRows*boardColumns];
        territoryArmiesButtons = new Button[boardRows*boardColumns];

        boardAnchorPane.getChildren().clear();
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setGridLinesVisible(true);

        for (int row = 0; row < boardRows; row++) {

            if(row > 0)
                boardGrid.addRow(row);

            for (int column = 0; column < boardColumns; column++) {

                if(column > 0)
                    boardGrid.addColumn(column);

                int territoryIndex = (row * boardColumns) + column;
                Territory currentTerritory = boardTerritories[territoryIndex];

                VBox territoryVBox = new VBox();
                territoryVBox.setAlignment(Pos.CENTER_LEFT);

                Label numberLabel = new Label("Territory #" + currentTerritory.getId());
                Label profitLabel = new Label("Profit: " + currentTerritory.getProfit());
                Label armyThresholdLabel = new Label("Threshold: " + currentTerritory.getArmyThreshold());
                Label conqueredLabel = new Label();
                conqueredLabel.textProperty().bind(currentTerritory.getConquerorTextProperty());

                HBox territoryButtonContainer = new HBox();
                territoryButtonContainer.setAlignment(Pos.CENTER);
                territoryButtonContainer.setSpacing(5);

                Button actionButton = new Button();
                territoryActionButtons[row*boardColumns + column] = actionButton;
                actionButton.setVisible(false);
                actionButton.setManaged(false);

                Button showArmiesButton = new Button("Armies");
                territoryArmiesButtons[row*boardColumns + column] = showArmiesButton;
                showArmiesButton.setVisible(false);
                showArmiesButton.setManaged(false);

                territoryButtonContainer.getChildren().addAll(actionButton, showArmiesButton);

                territoryVBox.getChildren().addAll(numberLabel, profitLabel, armyThresholdLabel, conqueredLabel, territoryButtonContainer);
                territoryVBox.setMinWidth(100);
                territoryVBox.setFillWidth(true);
                territoryVBox.setSpacing(10);
                territoryVBox.setPadding(new Insets(10));

                boardGrid.add(territoryVBox, column, row);
            }
        }

        boardAnchorPane.getChildren().add(boardGrid);
        AnchorPane.setTopAnchor(boardGrid, 5.0);
        AnchorPane.setRightAnchor(boardGrid, 5.0);
        AnchorPane.setBottomAnchor(boardGrid, 5.0);
        AnchorPane.setLeftAnchor(boardGrid, 5.0);
    }

}
