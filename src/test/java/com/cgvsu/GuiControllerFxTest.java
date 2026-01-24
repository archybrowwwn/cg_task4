package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.math.Vector3f;
import com.cgvsu.scene.SceneObject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class GuiControllerFxTest {

    private static boolean fxStarted = false;

    @BeforeAll
    static void initJavaFx() throws Exception {
        Locale.setDefault(Locale.US);

        if (fxStarted) return;

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX Platform did not start in time");
            fxStarted = true;
        } catch (IllegalStateException alreadyStarted) {
            fxStarted = true;
        }
    }

    private static <T> T runFxAndWait(final FxSupplier<T> supplier) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final Holder<T> holder = new Holder<>();
        final Holder<Throwable> error = new Holder<>();

        Platform.runLater(() -> {
            try {
                holder.value = supplier.get();
            } catch (Throwable t) {
                error.value = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for FX task");
        if (error.value != null) {
            if (error.value instanceof Exception e) throw e;
            throw new RuntimeException(error.value);
        }
        return holder.value;
    }

    private static void runFxAndWait(final Runnable r) throws Exception {
        runFxAndWait(() -> { r.run(); return null; });
    }

    @FunctionalInterface
    private interface FxSupplier<T> { T get() throws Exception; }

    private static final class Holder<T> { T value; }

    private static FXMLLoader fxmlLoader() {
        URL fxml = GuiControllerFxTest.class.getResource("/com/cgvsu/fxml/gui.fxml");
        assertNotNull(fxml, "gui.fxml not found in resources");
        return new FXMLLoader(fxml);
    }

    private static GuiController loadControllerOnFxThread() throws Exception {
        return runFxAndWait(() -> {
            FXMLLoader loader = fxmlLoader();
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));

            return loader.getController();
        });
    }

    private static <T> T getPrivateField(final Object target, final String name, final Class<T> type) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object v = f.get(target);
        return type.cast(v);
    }

    private static void setPrivateField(final Object target, final String name, final Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void invokePrivate(final Object target, final String name) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name);
        m.setAccessible(true);
        m.invoke(target);
    }

    @Test
    public void fxmlLoads_andControlsInjected() throws Exception {
        GuiController controller = loadControllerOnFxThread();

        Slider tx = getPrivateField(controller, "txSlider", Slider.class);
        Slider ry = getPrivateField(controller, "rySlider", Slider.class);
        Label txLabel = getPrivateField(controller, "txValueLabel", Label.class);

        assertNotNull(tx);
        assertNotNull(ry);
        assertNotNull(txLabel);
    }

    @Test
    public void applyTransformFromSliders_updatesActiveSceneObject_andLabels() throws Exception {
        GuiController controller = loadControllerOnFxThread();

        SceneObject obj = new SceneObject(new Model(), "dummy");
        obj.position = new Vector3f(0, 0, 0);
        obj.rotationDeg = new Vector3f(0, 0, 0);
        obj.scale = new Vector3f(1, 1, 1);

        ArrayList<SceneObject> list = new ArrayList<>();
        list.add(obj);

        setPrivateField(controller, "sceneObjects", list);
        setPrivateField(controller, "activeIndex", 0);

        Slider tx = getPrivateField(controller, "txSlider", Slider.class);
        Slider ty = getPrivateField(controller, "tySlider", Slider.class);
        Slider tz = getPrivateField(controller, "tzSlider", Slider.class);

        Slider rx = getPrivateField(controller, "rxSlider", Slider.class);
        Slider ry = getPrivateField(controller, "rySlider", Slider.class);
        Slider rz = getPrivateField(controller, "rzSlider", Slider.class);

        Slider sx = getPrivateField(controller, "sxSlider", Slider.class);
        Slider sy = getPrivateField(controller, "sySlider", Slider.class);
        Slider sz = getPrivateField(controller, "szSlider", Slider.class);

        Label txLabel = getPrivateField(controller, "txValueLabel", Label.class);
        Label ryLabel = getPrivateField(controller, "ryValueLabel", Label.class);
        Label szLabel = getPrivateField(controller, "szValueLabel", Label.class);

        runFxAndWait(() -> {
            tx.setValue(10.25);
            ty.setValue(-3.5);
            tz.setValue(1.0);

            rx.setValue(30.0);
            ry.setValue(-45.0);
            rz.setValue(90.0);

            sx.setValue(2.0);
            sy.setValue(0.5);
            sz.setValue(3.25);
        });

        invokePrivate(controller, "applyTransformFromSliders");

        assertEquals(10.25f, obj.position.x, 1e-6);
        assertEquals(-3.5f, obj.position.y, 1e-6);
        assertEquals(1.0f, obj.position.z, 1e-6);

        assertEquals(30.0f, obj.rotationDeg.x, 1e-6);
        assertEquals(-45.0f, obj.rotationDeg.y, 1e-6);
        assertEquals(90.0f, obj.rotationDeg.z, 1e-6);

        assertEquals(2.0f, obj.scale.x, 1e-6);
        assertEquals(0.5f, obj.scale.y, 1e-6);
        assertEquals(3.25f, obj.scale.z, 1e-6);

        assertEquals("10.25", txLabel.getText());
        assertEquals("-45.0", ryLabel.getText());
        assertEquals("3.25", szLabel.getText());
    }

    @Test
    public void syncSlidersWithActiveObject_setsSlidersFromObject_andLabels() throws Exception {
        GuiController controller = loadControllerOnFxThread();

        SceneObject obj = new SceneObject(new Model(), "dummy");
        obj.position = new Vector3f(5.5f, -2.25f, 7.0f);
        obj.rotationDeg = new Vector3f(10.0f, 20.0f, 30.0f);
        obj.scale = new Vector3f(1.1f, 2.2f, 3.3f);

        ArrayList<SceneObject> list = new ArrayList<>();
        list.add(obj);

        setPrivateField(controller, "sceneObjects", list);
        setPrivateField(controller, "activeIndex", 0);

        runFxAndWait(() -> {
            invokePrivate(controller, "syncSlidersWithActiveObject");
            return null;
        });

        Slider tx = getPrivateField(controller, "txSlider", Slider.class);
        Slider ry = getPrivateField(controller, "rySlider", Slider.class);
        Slider sz = getPrivateField(controller, "szSlider", Slider.class);

        Label tyLabel = getPrivateField(controller, "tyValueLabel", Label.class);
        Label ryLabel = getPrivateField(controller, "ryValueLabel", Label.class);
        Label szLabel = getPrivateField(controller, "szValueLabel", Label.class);

        assertEquals(5.5, tx.getValue(), 1e-6);
        assertEquals(20.0, ry.getValue(), 1e-6);
        assertEquals(3.3, sz.getValue(), 1e-6);

        assertEquals("-2.25", tyLabel.getText());
        assertEquals("20.0", ryLabel.getText());
        assertEquals("3.30", szLabel.getText());
    }
}
