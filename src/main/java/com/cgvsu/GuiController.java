package com.cgvsu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// [Артём] математика
import com.cgvsu.math.Vector3f;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.objwriter.ObjWriterException;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;

// [Илья] Сохранение
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.objwriter.ObjWriterException;

// [Дима] Обработка модели
import com.cgvsu.model.ModelPreprocessor;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;


import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            // [Дима] Триангуляция и пересчет нормалей
            // Это критически важно для работы Z-буфера
            try {
                ModelPreprocessor.triangulate(mesh);
                ModelPreprocessor.recalculateNormals(mesh);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException exception) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load model");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    // [Илья] Метод сохранения модели (Обновленная версия)
    @FXML
    private void onSaveModelMenuItemClick() {
        if (mesh == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Save");
            alert.setHeaderText("No model loaded");
            alert.setContentText("Load a model first.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        try {
            String objText = ObjWriter.write(mesh);
            Files.writeString(file.toPath(), objText);
        } catch (ObjWriterException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save error");
            alert.setHeaderText("Failed to save OBJ");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save error");
            alert.setHeaderText("Failed to write file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // [Артём] Управление камерой
    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    private void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    private void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    private void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    private void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    private void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    // [Артём] Трансформация модели
    @FXML
    private void handleModelScaleUp(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f s = mesh.getScale();
            mesh.setScale(new Vector3f(s.x * 1.1f, s.y * 1.1f, s.z * 1.1f));
        }
    }

    @FXML
    private void handleModelScaleDown(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f s = mesh.getScale();
            mesh.setScale(new Vector3f(s.x * 0.9f, s.y * 0.9f, s.z * 0.9f));
        }
    }

    @FXML
    private void handleModelRotateLeft(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f r = mesh.getRotation();
            mesh.setRotation(new Vector3f(r.x, r.y, r.z - 5));
        }
    }

    @FXML
    private void handleModelRotateRight(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f r = mesh.getRotation();
            mesh.setRotation(new Vector3f(r.x, r.y, r.z + 5));
        }
    }

    @FXML
    private void handleModelTranslateForward(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f t = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(t.x, t.y, t.z - 0.5f));
        }
    }

    @FXML
    private void handleModelTranslateBackward(ActionEvent actionEvent) {
        if (mesh != null) {
            Vector3f t = mesh.getTranslation();
            mesh.setTranslation(new Vector3f(t.x, t.y, t.z + 0.5f));
        }
    }
}