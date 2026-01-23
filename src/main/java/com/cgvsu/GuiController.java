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
import com.cgvsu.scene.SceneObject;
import javafx.scene.input.KeyEvent;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

public class GuiController {

    private static final float TRANSLATION = 0.5F;

    @FXML
    private Canvas canvas;

    @FXML
    private ListView<String> modelsListView;

    @FXML
    private ListView<String> polygonsListView;

    @FXML
    private Slider txSlider;
    @FXML
    private Slider tySlider;
    @FXML
    private Slider tzSlider;
    @FXML
    private Slider rxSlider;
    @FXML
    private Slider rySlider;
    @FXML
    private Slider rzSlider;
    @FXML
    private Slider sxSlider;
    @FXML
    private Slider sySlider;
    @FXML
    private Slider szSlider;

    @FXML
    private Label txValueLabel;
    @FXML
    private Label tyValueLabel;
    @FXML
    private Label tzValueLabel;
    @FXML
    private Label rxValueLabel;
    @FXML
    private Label ryValueLabel;
    @FXML
    private Label rzValueLabel;
    @FXML
    private Label sxValueLabel;
    @FXML
    private Label syValueLabel;
    @FXML
    private Label szValueLabel;

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

    private Image texture = null;

    private final ArrayList<Camera> cameras = new ArrayList<>();
    private int activeCameraIndex = 0;

    private Timeline timeline;

    // [Артём [Task 5]] Переменные для мыши
    private float lastX;
    private float lastY;
    private final float MOUSE_SENSITIVITY = 0.01f;

    private boolean isSyncingSliders = false;

    private SceneObject getActiveObject() {
        if (activeIndex < 0 || activeIndex >= sceneObjects.size()) return null;
        return sceneObjects.get(activeIndex);
    }

    private static String fmt2(final float v) {
        return String.format("%.2f", v);
    }

    private static String fmt1(final float v) {
        return String.format("%.1f", v);
    }

    private void setupTransformSliders() {
        if (txSlider == null) return;

        configureSlider(txSlider, -50, 50, 0);
        configureSlider(tySlider, -50, 50, 0);
        configureSlider(tzSlider, -50, 50, 0);

        configureSlider(rxSlider, -180, 180, 0);
        configureSlider(rySlider, -180, 180, 0);
        configureSlider(rzSlider, -180, 180, 0);

        configureSlider(sxSlider, 0.01, 10, 1);
        configureSlider(sySlider, 0.01, 10, 1);
        configureSlider(szSlider, 0.01, 10, 1);

        txSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        tySlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        tzSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());

        rxSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        rySlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        rzSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());

        sxSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        sySlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());
        szSlider.valueProperty().addListener((o, ov, nv) -> applyTransformFromSliders());

        syncSlidersWithActiveObject();
    }

    private void configureSlider(final Slider s, final double min, final double max, final double value) {
        if (s == null) return;
        s.setMin(min);
        s.setMax(max);
        s.setValue(value);
        s.setBlockIncrement((max - min) / 200.0);
        s.setMajorTickUnit((max - min) / 4.0);
        s.setMinorTickCount(4);
        s.setSnapToTicks(false);
    }

    private void syncSlidersWithActiveObject() {
        if (txSlider == null) return;
        SceneObject active = getActiveObject();
        if (active == null) return;

        isSyncingSliders = true;
        try {
            txSlider.setValue(active.position.x);
            tySlider.setValue(active.position.y);
            tzSlider.setValue(active.position.z);

            rxSlider.setValue(active.rotationDeg.x);
            rySlider.setValue(active.rotationDeg.y);
            rzSlider.setValue(active.rotationDeg.z);

            sxSlider.setValue(active.scale.x);
            sySlider.setValue(active.scale.y);
            szSlider.setValue(active.scale.z);

            updateTransformValueLabels(active);
        } finally {
            isSyncingSliders = false;
        }
    }

    private void applyTransformFromSliders() {
        if (isSyncingSliders || txSlider == null) return;
        SceneObject active = getActiveObject();
        if (active == null) return;

        active.position.x = (float) txSlider.getValue();
        active.position.y = (float) tySlider.getValue();
        active.position.z = (float) tzSlider.getValue();

        active.rotationDeg.x = (float) rxSlider.getValue();
        active.rotationDeg.y = (float) rySlider.getValue();
        active.rotationDeg.z = (float) rzSlider.getValue();

        active.scale.x = (float) sxSlider.getValue();
        active.scale.y = (float) sySlider.getValue();
        active.scale.z = (float) szSlider.getValue();

        updateTransformValueLabels(active);
    }

    private void updateTransformValueLabels(final SceneObject active) {
        if (txValueLabel != null) txValueLabel.setText(fmt2(active.position.x));
        if (tyValueLabel != null) tyValueLabel.setText(fmt2(active.position.y));
        if (tzValueLabel != null) tzValueLabel.setText(fmt2(active.position.z));

        if (rxValueLabel != null) rxValueLabel.setText(fmt1(active.rotationDeg.x));
        if (ryValueLabel != null) ryValueLabel.setText(fmt1(active.rotationDeg.y));
        if (rzValueLabel != null) rzValueLabel.setText(fmt1(active.rotationDeg.z));

        if (sxValueLabel != null) sxValueLabel.setText(fmt2(active.scale.x));
        if (syValueLabel != null) syValueLabel.setText(fmt2(active.scale.y));
        if (szValueLabel != null) szValueLabel.setText(fmt2(active.scale.z));
    }

    @FXML
    private void onResetTransform(final ActionEvent e) {
        SceneObject active = getActiveObject();
        if (active == null) return;
        active.position.x = 0;
        active.position.y = 0;
        active.position.z = 0;
        active.rotationDeg.x = 0;
        active.rotationDeg.y = 0;
        active.rotationDeg.z = 0;
        active.scale.x = 1;
        active.scale.y = 1;
        active.scale.z = 1;
        syncSlidersWithActiveObject();
    }

    private void refreshPolygonsList() {
        polygonsListView.getItems().clear();

        SceneObject active = getActiveObject();
        if (active == null) return;

        Model m = active.getModel();
        if (m == null) return;

        for (int i = 0; i < m.polygons.size(); i++) {
            polygonsListView.getItems().add("Polygon #" + i);
        }
    }

    @FXML
    private void initialize() {
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;

            newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!event.isControlDown()) return;

                if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.ADD) {
                    handleCameraForward(null);
                    event.consume(); // важно: глушим, чтобы accelerator не сработал
                } else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                    handleCameraBackward(null);
                    event.consume();
                }
            });

            if (canvas.getParent() instanceof StackPane sp) {
                canvas.widthProperty().bind(sp.widthProperty());
                canvas.heightProperty().bind(sp.heightProperty());
            }
        });

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

        setupTransformSliders();

        if (modelsListView != null) {
            modelsListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                int idx = newVal.intValue();
                if (idx >= 0 && idx < sceneObjects.size()) {
                    activeIndex = idx;
                    refreshPolygonsList();
                    syncSlidersWithActiveObject();
                }
            });
        }

        // [Артём [Task 5]] Обработка клика мыши
        canvas.setFocusTraversable(true);
        canvas.setOnMousePressed(event -> {
            lastX = (float) event.getX();
            lastY = (float) event.getY();
        });

        canvas.setOnMouseDragged(event -> {
            float x = (float) event.getX();
            float y = (float) event.getY();

            float dx = (x - lastX) * MOUSE_SENSITIVITY;
            float dy = (y - lastY) * MOUSE_SENSITIVITY;

            if (event.getButton() == MouseButton.PRIMARY) {
                SceneObject active = getActiveObject();
                if (active != null) {
                    active.rotationDeg.y += (float) Math.toDegrees(dx);
                    active.rotationDeg.x += (float) Math.toDegrees(dy);
                    syncSlidersWithActiveObject();
                }
            } else if (event.getButton() == MouseButton.SECONDARY) {
                handleCameraRotation(dx, dy);
            }

            lastX = x;
            lastY = y;
        });

        // [Артём [Task 5]] Вращение камеры мышью
        canvas.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.W) handleCameraForward(null);
            else if (event.getCode() == KeyCode.S) handleCameraBackward(null);

            else if (event.getCode() == KeyCode.A) {
                SceneObject active = getActiveObject();
                if (active != null) active.rotationDeg.y += 5.0f;
            } else if (event.getCode() == KeyCode.D) {
                SceneObject active = getActiveObject();
                if (active != null) active.rotationDeg.y -= 5.0f;
            } else if (event.getCode() == KeyCode.UP) handleCameraUp(null);
            else if (event.getCode() == KeyCode.DOWN) handleCameraDown(null);
            else if (event.getCode() == KeyCode.LEFT) handleCameraLeft(null);
            else if (event.getCode() == KeyCode.RIGHT) handleCameraRight(null);
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            if (width <= 1 || height <= 1) return;

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            getActiveCamera().setAspectRatio((float) (width / height));

            for (SceneObject obj : sceneObjects) {
                Matrix4f modelMatrix = rotateScaleTranslate(obj.position, obj.rotationDeg, obj.scale);

                boolean drawWireframe = cbWireframe != null && cbWireframe.isSelected();
                boolean useTexture = cbTexture != null && cbTexture.isSelected();
                boolean useLighting = cbLighting != null && cbLighting.isSelected();

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

            refreshPolygonsList();

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

    @FXML
    private void onDeleteSelectedPolygon() {
        try {
            if (polygonsListView == null) {
                Alert a = new Alert(Alert.AlertType.ERROR, "polygonsListView is null (fx:id mismatch).");
                a.showAndWait();
                return;
            }

            SceneObject active = getActiveObject();
            if (active == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "No active model.");
                a.showAndWait();
                return;
            }

            Model m = active.getModel();
            if (m == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Active model is null.");
                a.showAndWait();
                return;
            }

            int idx = polygonsListView.getSelectionModel().getSelectedIndex();

            if (idx < 0) {
                String item = polygonsListView.getSelectionModel().getSelectedItem();
                if (item != null && item.startsWith("Polygon #")) {
                    try {
                        idx = Integer.parseInt(item.substring("Polygon #".length()).trim());
                    } catch (NumberFormatException ignored) {
                        idx = -1;
                    }
                }
            }

            if (idx < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Delete Polygon");
                alert.setHeaderText("No polygon selected");
                alert.setContentText("Select a polygon in the list first.");
                alert.showAndWait();
                return;
            }

            int before = m.polygons.size();

            com.cgvsu.model.ModelEditor.deletePolygon(m, idx);

            int after = m.polygons.size();

            try {
                ModelPreprocessor.recalculateNormals(m);
            } catch (Exception e) {
                e.printStackTrace();
            }

            refreshPolygonsList();

            if (after > 0) {
                int select = idx;
                if (select >= after) select = after - 1;
                polygonsListView.getSelectionModel().select(select);
            }

            System.out.println("Deleted polygon " + idx + " | polygons: " + before + " -> " + after);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete Polygon Error");
            alert.setHeaderText("Exception while deleting polygon");
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    // Переключение активной модели (для пункта 2)
    @FXML
    private void onSelectNextModel() {
        if (sceneObjects.isEmpty()) return;
        activeIndex = (activeIndex + 1) % sceneObjects.size();
        modelsListView.getSelectionModel().select(activeIndex);
        refreshPolygonsList();
    }

    @FXML
    private void onSelectPrevModel() {
        if (sceneObjects.isEmpty()) return;
        activeIndex = (activeIndex - 1 + sceneObjects.size()) % sceneObjects.size();
        modelsListView.getSelectionModel().select(activeIndex);
        refreshPolygonsList();
    }

    // !!! [Артём] ОБНОВЛЕНИЕ Управление камерой

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    private void handleCameraBackward(ActionEvent actionEvent) {
        getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraTurnLeft(ActionEvent actionEvent) {
        handleCameraRotation(-0.05f, 0);
    }

    @FXML
    public void handleCameraTurnRight(ActionEvent actionEvent) {
        handleCameraRotation(0.05f, 0);
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


    // !!! [Артём] Новый метод

    private void handleCameraRotation(float dYaw, float dPitch) {
        Camera camera = getActiveCamera();
        Vector3f target = camera.getTarget();
        Vector3f position = camera.getPosition();
        Vector3f view = Vector3f.subtract(target, position);
        Matrix4f rotateY = Matrix4f.rotateY(dYaw);
        Matrix4f rotateX = Matrix4f.rotateX(dPitch);

        Vector3f v = Matrix4f.multiply(rotateY, view);
        v = Matrix4f.multiply(rotateX, v);
        camera.setTarget(position.add(v));
    }


    @FXML
    private void handleModelScaleUp(ActionEvent actionEvent) {
        SceneObject active = getActiveObject();
        if (active == null) return;

        Vector3f s = active.scale;
        active.scale.x = s.x * 1.1f;
        active.scale.y = s.y * 1.1f;
        active.scale.z = s.z * 1.1f;

        handleCameraForward(null);

        handleCameraBackward(null);
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