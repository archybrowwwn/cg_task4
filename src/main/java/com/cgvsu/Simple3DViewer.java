package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import javafx.scene.Parent;
import java.util.Objects;


public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Simple3DViewer.class.getResource("/com/cgvsu/fxml/gui.fxml")));


        Scene scene = new Scene(root);

        stage.setMinWidth(1600);
        stage.setMinHeight(900);



        stage.setTitle("Simple3DViewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}