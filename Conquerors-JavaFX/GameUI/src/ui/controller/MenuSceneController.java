package ui.controller;

import engine.GameData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import ui.MainApp;

import java.io.File;
import java.util.Stack;

public class MenuSceneController implements SceneController {

    @FXML private Label gameDataLoadedLabel;
    @FXML private ProgressBar gameDataLoadedProgressBar;

    private MainApp mainApp;

    public MenuSceneController(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleLoadGame(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "DAT Files (*.dat)", "*.dat");
        fileChooser.getExtensionFilters().add(extFilter);
        File fileToLoad = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        GameData currentGameData = mainApp.getGameData();
        if(fileToLoad != null){
            if (currentGameData == null || !currentGameData.isValidGameConfig())
                currentGameData = new GameData();

            String loadGameErrors = currentGameData.loadGame(fileToLoad);

            if (loadGameErrors != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error!");
                alert.setHeaderText("Game loading failed due to the following errors:");
                alert.setContentText(loadGameErrors);

                alert.show();
            } else {
                mainApp.setGameData(currentGameData);
                mainApp.popupAlert(mainApp.getPrimaryStage(), "Game Loaded Successfully!", Alert.AlertType.INFORMATION);
                mainApp.toggleMenuGameScenes();
            }
        }
    }

    @FXML
    private void handleQuit(){
        Platform.exit();
    }

    @FXML
    private void handleLoadXML() {

        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        Task<GameData> xmlLoadTask = new LoadGameDataFromXMLTask(file);

        // task message
        gameDataLoadedLabel.textProperty().bind(xmlLoadTask.messageProperty());

        // task progress bar
        gameDataLoadedProgressBar.setVisible(true);
        gameDataLoadedProgressBar.progressProperty().bind(xmlLoadTask.progressProperty());

        // task cleanup upon finish
        xmlLoadTask.valueProperty().addListener((observable, oldValue, newValue) -> onFinishXMLLoad(newValue));

        new Thread(xmlLoadTask).start();
    }

    private void onFinishXMLLoad(GameData loadedGameData){

        if(loadedGameData != null) {
            gameDataLoadedLabel.textProperty().unbind();
            gameDataLoadedProgressBar.progressProperty().unbind();
            mainApp.setGameData(loadedGameData);

            if (loadedGameData.isValidGameConfig()) {
                gameDataLoadedLabel.setText("Game Data Loaded");

                mainApp.popupAlert(mainApp.getPrimaryStage(), "XML File Loaded Successfully!", Alert.AlertType.INFORMATION);
                mainApp.toggleMenuGameScenes();
            } else {

                Stack<String> gameMessages = mainApp.getGameData().getGameErrorMessages();

                StringBuilder errorMessages = new StringBuilder();
                for (String str : gameMessages) {
                    errorMessages.append("- ");
                    errorMessages.append(str);
                    errorMessages.append("\n");
                }

                gameDataLoadedLabel.setText("No Game Data Loaded");

                // Show the error message.
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error!");
                alert.setHeaderText("The following errors occurred during XML parsing of the game configuration");
                alert.setContentText(errorMessages.toString());

                alert.show();
            }
        } else {
            mainApp.popupAlert(mainApp.getPrimaryStage(), "There was a problem with the file you tried to load", Alert.AlertType.ERROR);
        }
    }

    private class LoadGameDataFromXMLTask extends Task<GameData> {

        private File fileToLoad;

        public LoadGameDataFromXMLTask(File fileToLoad){
            this.fileToLoad = fileToLoad;
        }

        @Override
        protected GameData call() throws Exception {
            try {
                if (fileToLoad != null) {
                    updateMessage("Parsing XML...");
                    GameData currentGameData = new GameData(fileToLoad);

                    // Simulate progress bar progression
                    for (int i = 0; i <= 10; i++) {
                        updateProgress(i * 10, 100);
                        Thread.sleep(25);
                    }
                    updateProgress(0, 100);
                    return currentGameData;
                } else {
                    updateMessage("No Game Data Loaded.");
                    updateProgress(0, 100);
                    return null;
                }
            } catch(Exception e) {
                throw e;
            }
        }
    }
}
