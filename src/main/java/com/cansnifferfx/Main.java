package com.cansnifferfx;

import com.cansnifferfx.controllers.SampleController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static final String FXML_MAIN = "/fxml/sample.fxml";
    private Stage primaryStage;             //сцена главного окна
    private SampleController mainController;//контроллер сцены главного окна
    private FXMLLoader fxmlLoader;          //загрузчик файлов FXML
    private GridPane currentRoot;           //коренной Node (Parent)

    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        createGUI();
    }

    private void createGUI() {
        currentRoot = loadFXML(); //возвращает сконфигурированный Node (Parent)
        Scene scene = new Scene(currentRoot, 760, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(760);
        primaryStage.show();
    }

    private GridPane loadFXML() {   //возвращает сконфигурированный Node (Parent)
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(FXML_MAIN));
        GridPane node = null;

        try {
            node = (GridPane) fxmlLoader.load();
            mainController = fxmlLoader.getController();
            primaryStage.setTitle("CANSnifferFX 0.1.0");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
