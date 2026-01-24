package com.cgvsu.RenderEngine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RenderEngineTest {

    private static volatile boolean fxStarted = false;

    @BeforeAll
    static void initJavaFx() throws Exception {
        if (fxStarted) return;

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX Platform failed to start");
            }
        } catch (IllegalStateException alreadyStarted) {

        }

        fxStarted = true;
    }

    private static void runOnFxThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err.set(t);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e);
        }
        if (err.get() != null) {
            Throwable t = err.get();
            if (t instanceof RuntimeException re) throw re;
            throw new RuntimeException(t);
        }
    }

    private static <T> T callOnFxThread(Callable<T> c) {
        AtomicReference<T> res = new AtomicReference<>();
        runOnFxThread(() -> {
            try {
                res.set(c.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return res.get();
    }


    private static Camera newCameraOrFail(int w, int h) {
        try {
            Vector3f position = new Vector3f(0, 0, 3);
            Vector3f target = new Vector3f(0, 0, 0);
            Vector3f up = new Vector3f(0, 1, 0);

            float aspect = (h == 0) ? 1.0f : (float) w / (float) h;
            float near = 0.1f;
            float far = 100.0f;

            float fov = (float) Math.toRadians(60);

            for (Constructor<?> c : Camera.class.getDeclaredConstructors()) {
                c.setAccessible(true);
                Class<?>[] p = c.getParameterTypes();

                if (p.length == 2 && p[0] == Vector3f.class && p[1] == Vector3f.class) {
                    return (Camera) c.newInstance(position, target);
                }

                if (p.length == 3 && p[0] == Vector3f.class && p[1] == Vector3f.class && p[2] == Vector3f.class) {
                    return (Camera) c.newInstance(position, target, up);
                }

                if (p.length == 6 && p[0] == Vector3f.class && p[1] == Vector3f.class
                        && p[2] == float.class && p[3] == float.class && p[4] == float.class && p[5] == float.class) {
                    return (Camera) c.newInstance(position, target, fov, aspect, near, far);
                }

                if (p.length == 7 && p[0] == Vector3f.class && p[1] == Vector3f.class && p[2] == Vector3f.class
                        && p[3] == float.class && p[4] == float.class && p[5] == float.class && p[6] == float.class) {
                    return (Camera) c.newInstance(position, target, up, fov, aspect, near, far);
                }
            }

            Constructor<?> any = Camera.class.getDeclaredConstructors()[0];
            any.setAccessible(true);
            Camera cam = (Camera) any.newInstance(new Object[any.getParameterCount()]);

            invokeIfExists(cam, "setPosition", Vector3f.class, position);
            invokeIfExists(cam, "setTarget", Vector3f.class, target);
            invokeIfExists(cam, "setUp", Vector3f.class, up);

            invokeIfExists(cam, "setFov", float.class, fov);
            invokeIfExists(cam, "setFieldOfView", float.class, fov);

            invokeIfExists(cam, "setAspectRatio", float.class, aspect);
            invokeIfExists(cam, "setAspect", float.class, aspect);

            invokeIfExists(cam, "setNearPlane", float.class, near);
            invokeIfExists(cam, "setNear", float.class, near);

            invokeIfExists(cam, "setFarPlane", float.class, far);
            invokeIfExists(cam, "setFar", float.class, far);

            return cam;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void invokeIfExists(Object obj, String methodName, Class<?> paramType, Object arg) {
        try {
            Method m = obj.getClass().getMethod(methodName, paramType);
            m.setAccessible(true);
            m.invoke(obj, arg);
        } catch (NoSuchMethodException ignored) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Model modelTriangleNoNormalsNoUV() {
        Model m = new Model();

        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.normals = new ArrayList<>();
        if (m.textureVertices != null) {
            m.textureVertices = new ArrayList<>();
        }

        m.vertices.add(new Vector3f(-0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.0f, 0.5f, 0.0f));

        Polygon p = new Polygon();
        p.setVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        m.polygons.add(p);

        return m;
    }

    private static Model modelTriangleWithNormalsAndUV() {
        Model m = new Model();

        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.normals = new ArrayList<>();
        if (m.textureVertices != null) {
            m.textureVertices = new ArrayList<>();
        }

        m.vertices.add(new Vector3f(-0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.0f, 0.5f, 0.0f));

        m.normals.add(new Vector3f(0, 0, 1));
        m.normals.add(new Vector3f(0, 0, 1));
        m.normals.add(new Vector3f(0, 0, 1));

        if (m.textureVertices != null) {
            m.textureVertices.add(new Vector2f(0.0f, 0.0f));
            m.textureVertices.add(new Vector2f(1.0f, 0.0f));
            m.textureVertices.add(new Vector2f(0.5f, 1.0f));
        }

        Polygon p = new Polygon();
        p.setVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        p.setNormalIndices(new ArrayList<>(List.of(0, 1, 2)));
        if (m.textureVertices != null) {
            p.setTextureVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        }
        m.polygons.add(p);

        return m;
    }

    private static WritableImage renderToImage(
            Model model,
            javafx.scene.image.Image texture,
            boolean useTexture,
            boolean useLighting,
            boolean drawWireframe,
            Color baseColor,
            int w,
            int h) {

        return callOnFxThread(() -> {
            Canvas canvas = new Canvas(w, h);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            Camera cam = newCameraOrFail(w, h);
            Matrix4f modelMatrix = Matrix4f.identity();

            RenderEngine.render(gc, cam, model, texture, useTexture, useLighting, drawWireframe, baseColor, w, h, modelMatrix);

            WritableImage out = new WritableImage(w, h);
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            canvas.snapshot(sp, out);
            return out;
        });
    }

    private static boolean anyPixelMatches(WritableImage img, java.util.function.Predicate<Color> pred) {
        PixelReader pr = img.getPixelReader();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (pred.test(pr.getColor(x, y))) return true;
            }
        }
        return false;
    }

    private static boolean allPixelsTransparent(WritableImage img) {
        PixelReader pr = img.getPixelReader();
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (pr.getColor(x, y).getOpacity() > 0.0) return false;
            }
        }
        return true;
    }

    @Test
    void render_doesNotThrow_basicFlatFill() {
        Model m = modelTriangleNoNormalsNoUV();
        assertDoesNotThrow(() -> renderToImage(m, null, false, false, false, Color.RED, 128, 128));
    }

    @Test
    void render_drawsSomething_whenTrianglePresent_flatFill() {
        Model m = modelTriangleNoNormalsNoUV();
        WritableImage img = renderToImage(m, null, false, false, false, Color.RED, 128, 128);
        assertTrue(anyPixelMatches(img, c -> c.getOpacity() > 0.0));
    }

    @Test
    void render_doesNotDraw_whenOnlyNonTriangles() {
        Model m = new Model();
        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.normals = new ArrayList<>();
        if (m.textureVertices != null) {
            m.textureVertices = new ArrayList<>();
        }

        m.vertices.add(new Vector3f(-0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.5f, -0.5f, 0.0f));
        m.vertices.add(new Vector3f(0.5f, 0.5f, 0.0f));
        m.vertices.add(new Vector3f(-0.5f, 0.5f, 0.0f));

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(List.of(0, 1, 2, 3))); // 4 вершины — не треугольник
        m.polygons.add(quad);

        WritableImage img = renderToImage(m, null, false, false, false, Color.RED, 128, 128);
        assertTrue(allPixelsTransparent(img));
    }

    @Test
    void render_withWireframe_producesNonTransparentPixels() {
        Model m = modelTriangleNoNormalsNoUV();
        WritableImage img = renderToImage(m, null, false, false, true, Color.RED, 128, 128);
        assertTrue(anyPixelMatches(img, c -> c.getOpacity() > 0.0));
    }

    @Test
    void render_withTexture_producesNonTransparentPixels() {
        Model m = modelTriangleWithNormalsAndUV();
        WritableImage tex = new WritableImage(2, 2);
        tex.getPixelWriter().setColor(0, 0, Color.LIME);
        tex.getPixelWriter().setColor(1, 0, Color.LIME);
        tex.getPixelWriter().setColor(0, 1, Color.LIME);
        tex.getPixelWriter().setColor(1, 1, Color.LIME);

        WritableImage img = renderToImage(m, tex, true, false, false, Color.RED, 128, 128);
        assertTrue(anyPixelMatches(img, c -> c.getOpacity() > 0.0));
    }

    @Test
    void render_withLighting_producesNonTransparentPixels() {
        Model m = modelTriangleWithNormalsAndUV();
        WritableImage img = renderToImage(m, null, false, true, false, Color.RED, 128, 128);
        assertTrue(anyPixelMatches(img, c -> c.getOpacity() > 0.0));
    }

    @Test
    void render_withTextureAndLighting_producesNonTransparentPixels() {
        Model m = modelTriangleWithNormalsAndUV();
        WritableImage tex = new WritableImage(2, 2);
        tex.getPixelWriter().setColor(0, 0, Color.LIME);
        tex.getPixelWriter().setColor(1, 0, Color.LIME);
        tex.getPixelWriter().setColor(0, 1, Color.LIME);
        tex.getPixelWriter().setColor(1, 1, Color.LIME);

        WritableImage img = renderToImage(m, tex, true, true, false, Color.RED, 128, 128);
        assertTrue(anyPixelMatches(img, c -> c.getOpacity() > 0.0));
    }
}
