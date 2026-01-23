package com.cgvsu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javafx.scene.control.ListView;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelPreprocessor;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.objwriter.ObjWriterException;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;

import com.cgvsu.scene.SceneObject;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

public class GuiController {

    private static final float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private ListView<String> modelsListView;

    private final ArrayList<SceneObject> sceneObjects = new ArrayList<>();
    private int activeIndex = -1;
    @FXML
    private CheckBox cbWireframe;

    @FXML
    private CheckBox cbTexture;

    @FXML
    private CheckBox cbLighting;

    @FXML
    private ColorPicker fillColorPicker;

    private Model mesh = null;
    private Image texture = null;

    private final ArrayList<Camera> cameras = new ArrayList<>();
    private int activeCameraIndex = 0;

    private Timeline timeline;

    private SceneObject getActiveObject() {
        if (activeIndex < 0 || activeIndex >= sceneObjects.size()) return null;
        return sceneObjects.get(activeIndex);
    }

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        if (fillColorPicker != null) {
            fillColorPicker.setValue(Color.LIGHTGRAY);
        }

        if (cameras.isEmpty()) {
            cameras.add(new Camera(
                    new Vector3f(0, 0, 100),
                    new Vector3f(0, 0, 0),
                    1.0F, 1, 0.01F, 100));
            activeCameraIndex = 0;
        }

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            getActiveCamera().setAspectRatio((float) (width / height));

            for (SceneObject obj : sceneObjects) {
                Matrix4f modelMatrix = rotateScaleTranslate(obj.position, obj.rotationDeg, obj.scale);

                boolean drawWireframe = cbWireframe != null && cbWireframe.isSelected();
                boolean useTexture   = cbTexture != null && cbTexture.isSelected();
                boolean useLighting  = cbLighting != null && cbLighting.isSelected();

                Color baseColor = (fillColorPicker != null && fillColorPicker.getValue() != null)
                            ? fillColorPicker.getValue()
                            : Color.LIGHTGRAY;

                RenderEngine.render(
                            canvas.getGraphicsContext2D(),
                            getActiveCamera(),
                            obj.getModel(),
                            texture,
                            useTexture,
                            useLighting,
                            drawWireframe,
                            baseColor,
                            (int) width,
                            (int) height,
                            modelMatrix
                    );
            }
            RenderEngine.renderCameraIcons(
                    canvas.getGraphicsContext2D(),
                    getActiveCamera(),
                    cameras,
                    activeCameraIndex,
                    (int) width,
                    (int) height
            );

            modelsListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                int idx = newVal.intValue();
                if (idx >= 0 && idx < sceneObjects.size()) {
                    activeIndex = idx;
                }
            });
        });
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private Camera getActiveCamera() {
        if (cameras.isEmpty()) {
            cameras.add(new Camera(
                    new Vector3f(0, 0, 100),
                    new Vector3f(0, 0, 0),
                    1.0F, 1, 0.01F, 100));
            activeCameraIndex = 0;
        }
        if (activeCameraIndex < 0) activeCameraIndex = 0;
        if (activeCameraIndex >= cameras.size()) activeCameraIndex = cameras.size() - 1;
        return cameras.get(activeCameraIndex);
    }

    @FXML
    private void onAddCameraMenuItemClick() {
        Camera c = getActiveCamera();
        Vector3f pos = c.getPosition();
        Vector3f tgt = c.getTarget();

        cameras.add(new Camera(
                new Vector3f(pos.x + 5f, pos.y + 5f, pos.z + 5f),
                new Vector3f(tgt.x, tgt.y, tgt.z),
                c.getFov(),
                c.getAspectRatio(),
                c.getNearPlane(),
                c.getFarPlane()
        ));
        activeCameraIndex = cameras.size() - 1;
    }

    @FXML
    private void onRemoveCameraMenuItemClick() {
        if (cameras.size() <= 1) return;
        cameras.remove(activeCameraIndex);
        if (activeCameraIndex >= cameras.size()) activeCameraIndex = cameras.size() - 1;
    }

    @FXML
    private void onNextCameraMenuItemClick() {
        if (cameras.isEmpty()) return;
        activeCameraIndex = (activeCameraIndex + 1) % cameras.size();
    }

    @FXML
    private void onPrevCameraMenuItemClick() {
        if (cameras.isEmpty()) return;
        activeCameraIndex = (activeCameraIndex - 1 + cameras.size()) % cameras.size();
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
            Model loaded = ObjReader.read(fileContent);

            // [Дима] Триангуляция и пересчет нормалей
            // Это критически важно для работы Z-буфера
            try {
                ModelPreprocessor.triangulate(loaded);
                ModelPreprocessor.recalculateNormals(loaded);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SceneObject obj = new SceneObject(loaded, file.getName());
            sceneObjects.add(obj);
            activeIndex = sceneObjects.size() - 1;

            modelsListView.getItems().add(obj.getName());
            modelsListView.getSelectionModel().select(activeIndex);

        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load model");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onOpenTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        try {
            texture = new Image(file.toURI().toString());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load texture");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // [Илья] Метод сохранения активной модели
    @FXML
    private void onSaveModelMenuItemClick() {
        SceneObject active = getActiveObject();
        if (active == null) {
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
            String objText = ObjWriter.write(active.getModel());
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

    // Переключение активной модели (для пункта 2)
    @FXML
    private void onSelectNextModel() {
        if (sceneObjects.isEmpty()) return;
        activeIndex = (activeIndex + 1) % sceneObjects.size();
    }

    @FXML
    private void onSelectPrevModel() {
        if (sceneObjects.isEmpty()) return;
        activeIndex = (activeIndex - 1 + sceneObjects.size()) % sceneObjects.size();
    }

    // [Артём] Управление камерой
    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    private void handleCameraBackward(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    private void handleCameraLeft(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    private void handleCameraRight(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    private void handleCameraUp(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    private void handleCameraDown(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    // ===== Трансформации АКТИВНОЙ модели (пункт 2) =====

    @FXML
    private void handleModelScaleUp(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        Vector3f s = active.scale;
        active.scale.x = s.x * 1.1f;
        active.scale.y = s.y * 1.1f;
        active.scale.z = s.z * 1.1f;
    }

    @FXML
    private void handleModelScaleDown(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        Vector3f s = active.scale;
        active.scale.x = s.x * 0.9f;
        active.scale.y = s.y * 0.9f;
        active.scale.z = s.z * 0.9f;
    }

    @FXML
    private void handleModelRotateLeft(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        active.rotationDeg.z -= 5.0f;
    }

    @FXML
    private void handleModelRotateRight(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        active.rotationDeg.z += 5.0f;
    }

    @FXML
    private void handleModelTranslateForward(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        active.position.z -= 0.5f;
    }

    @FXML
    private void handleModelTranslateBackward(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        active.position.z += 0.5f;
    }
}