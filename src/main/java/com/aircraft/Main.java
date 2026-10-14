package com.aircraft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1280, 820);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("AEROINTEL | Aircraft Maintenance & Component Lifecycle Intelligence System");
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(700);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal Error starting JavaFX Application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
