package ui.controller;

import engine.Army;
import engine.Player;
import engine.Territory;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import ui.MainApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameOverDialogController implements SceneController {

    @FXML private TableView<Player> playerResultsTable;
    @FXML private TableColumn<Player, Number> playerIdColumn;
    @FXML private TableColumn<Player, String> playerNameColumn;
    @FXML private TableColumn<Player, Number> numberOfTerritoriesColumn;
    @FXML private TableColumn<Player, Number> totalTerritoriesProfitColumn;
    @FXML private Button closeButton;
    @FXML private Button replayButton;
    @FXML private Label winnerLabel;

    private MainApp mainAppReference;
    private Stage dialogStage;
    private boolean replayMode = false;
    private boolean showAnimations;
    private GridPane boardGrid;
    private ObservableList<Player> playerResultsData = FXCollections.observableArrayList();

    public GameOverDialogController(MainApp mainApp){
        this.mainAppReference = mainApp;
    }

    public void initialize(){

        playerIdColumn.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
        playerNameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        numberOfTerritoriesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOwnedTerritories().size()));
        totalTerritoriesProfitColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTerritoriesTotalProfit()));

        playerResultsTable.setItems(playerResultsData);
        playerResultsData.clear();
    }

    public void setPlayers(Player[] players){
        playerResultsData.addAll(players);
    }

    public void setWinningPlayers(HashSet<Player> winningPlayers) {

        StringBuilder winnersText = new StringBuilder();

        if(winningPlayers.size() > 1) {
            winnersText.append("It's a tie! The winners are:\n");

            for(Player player : winningPlayers)
                winnersText.append(player.getNameAndId()).append("\n");
        } else {
            for(Player player : winningPlayers)
                winnersText.append(player.getNameAndId()).append(" is the winner!");
        }

        if(showAnimations) {
            for (Player player : winningPlayers) {
                for (Territory territory : player.getOwnedTerritories()) {
                    VBox territoryVBox = (VBox) boardGrid.getChildren().get(territory.getId());
                    showWinningEffect(territoryVBox, 0.1f, 1000);
                }
            }
        }

        winnerLabel.setText(winnersText.toString());

        playerResultsTable.setRowFactory(playerResultsTable -> new TableRow<Player>() {
            @Override
            protected void updateItem(Player rowPlayer, boolean empty) {
                super.updateItem(rowPlayer, empty);

                if(rowPlayer != null) {

                    // Fix for CSS table-row-cell class not rendering with other themes
                    getStyleClass().remove("table-row-cell");

                    if (!isEmpty() && winningPlayers.contains(rowPlayer)) {
                        setBackground(new Background(new BackgroundFill(rowPlayer.getColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                        if(showAnimations)
                            showWinningEffect(this, 0.01f, 500);
                    }
                }
            }
        });
    }

    private void showWinningEffect(Node node, double scaleValue, int duration){
        Glow glow = new Glow();
        glow.setLevel(0.4);
        node.setEffect(glow);
        ScaleTransition st = new ScaleTransition(Duration.millis(duration), node);
        st.setByX(scaleValue);
        st.setByY(scaleValue);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);

        st.play();
    }

    public boolean isReplayMode(){
        return replayMode;
    }

    public void setBoardGrid(GridPane boardGrid){ this.boardGrid = boardGrid; }

    public void setShowAnimations(boolean showAnimations) { this.showAnimations = showAnimations; }

    @FXML
    private void handleReplay(){
        replayMode = true;
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleClose(){
        dialogStage.close();
    }
}
