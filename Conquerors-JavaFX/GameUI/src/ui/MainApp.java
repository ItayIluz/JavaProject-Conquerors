package ui;

import engine.GameData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.controller.GameSceneController;
import ui.controller.MenuSceneController;
import ui.controller.SceneController;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private GameData gameData;
    private boolean showMenuScene = true;
    private String currentTheme = "Default";

    private final String styleTheme1 = getClass().getResource("view/theme1.css").toString();
    private final String styleTheme2 = getClass().getResource("view/theme2.css").toString();

    public MainApp(){

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Conquerors");

        toggleMenuGameScenes();
    }

    public void toggleMenuGameScenes(){
        Scene sceneToShow;
        if(showMenuScene) {
            sceneToShow = loadNewScene("view/MenuScene.fxml", new MenuSceneController(this));
            showMenuScene = false;
        } else {
            sceneToShow = loadNewScene("view/GameScene.fxml", new GameSceneController(this));
            showMenuScene = true;
        }

        primaryStage.setScene(sceneToShow);
        primaryStage.show();
    }

    private Scene loadNewScene(String resourceName, SceneController sceneController){
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(resourceName));
            loader.setController(sceneController);
            ScrollPane sceneScrollPane = loader.load();

            // Show the scene containing the root layout.
            Scene newScene = new Scene(sceneScrollPane);
            changeTheme(currentTheme, newScene);

            return newScene;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Stage loadNewDialogScene(String resourceName, SceneController sceneController, String title, Stage owner){
        Scene dialogScene = loadNewScene(resourceName, sceneController);

        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setScene(dialogScene);

        return dialogStage;
    }

    public void popupAlert(Stage stage, String headerText, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        if(type == Alert.AlertType.INFORMATION)
            alert.setTitle("Success!");
        else
            alert.setTitle("Error!");

        alert.setHeaderText(headerText);

        alert.show();
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public GameData getGameData() {
        return gameData;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void changeTheme(String themeName, Scene scene){

        switch(themeName){
            case "Theme 1": {
                scene.getStylesheets().remove(styleTheme2);
                scene.getStylesheets().add(styleTheme1);
                break;
            } case "Theme 2": {
                scene.getStylesheets().remove(styleTheme1);
                scene.getStylesheets().add(styleTheme2);
                break;
            } default: {
                scene.getStylesheets().removeAll(styleTheme1, styleTheme2);
            }
        }
    }

    public void setCurrentTheme(String newTheme){
        if(!newTheme.equals(currentTheme)) {
            currentTheme = newTheme;
            changeTheme(currentTheme, getPrimaryStage().getScene());
        }
    }

    public String getCurrentTheme(){
        return currentTheme;
    }
}
