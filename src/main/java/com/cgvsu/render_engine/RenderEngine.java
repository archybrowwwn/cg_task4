package com.cgvsu.render_engine;

import java.util.ArrayList;
import java.util.Arrays;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Image texture,
            final boolean useTexture,
            final boolean useLighting,
            final boolean drawWireframe,
            final Color baseColor,
            final int width,
            final int height,
            final Matrix4f modelMatrix) {

        final float[] zBuffer = new float[width * height];
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();
        final PixelReader pixelReader = (texture != null) ? texture.getPixelReader() : null;

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = Matrix4f.multiply(viewMatrix, modelMatrix);
        Matrix4f modelViewProjectionMatrix = Matrix4f.multiply(projectionMatrix, modelViewMatrix);

        graphicsContext.setStroke(Color.BLACK);

        final int nPolygons = mesh.polygons.size();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final Polygon poly = mesh.polygons.get(polygonInd);
            final ArrayList<Integer> vIdx = poly.getVertexIndices();

            if (vIdx.size() != 3) continue;

            Vector3f v0 = mesh.vertices.get(vIdx.get(0));
            Vector3f v1 = mesh.vertices.get(vIdx.get(1));
            Vector3f v2 = mesh.vertices.get(vIdx.get(2));


            Vector3f p0ndc = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v0);
            Vector3f p1ndc = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v1);
            Vector3f p2ndc = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v2);


            Vector2f p0 = vertexToPoint(p0ndc, width, height);
            Vector2f p1 = vertexToPoint(p1ndc, width, height);
            Vector2f p2 = vertexToPoint(p2ndc, width, height);

            if (!useTexture && !useLighting) {
                rasterizeTriangle(
                        p0, p1, p2,
                        p0ndc.z, p1ndc.z, p2ndc.z,
                        pixelWriter, zBuffer, width, height,
                        baseColor
                );
            } else if (useTexture && !useLighting) {
                UVTriangle uvTri = getTriangleUV(mesh, poly);
                rasterizeTriangleTextured(
                        p0, p1, p2,
                        p0ndc.z, p1ndc.z, p2ndc.z,
                        uvTri,
                        texture, pixelReader,
                        pixelWriter, zBuffer, width, height,
                        baseColor
                );
            } else if (!useTexture && useLighting) {
                Vector3f v0View = Matrix4f.multiply(modelViewMatrix, v0);
                Vector3f v1View = Matrix4f.multiply(modelViewMatrix, v1);
                Vector3f v2View = Matrix4f.multiply(modelViewMatrix, v2);

                Vector3f n0 = getVertexNormal(mesh, poly, 0);
                Vector3f n1 = getVertexNormal(mesh, poly, 1);
                Vector3f n2 = getVertexNormal(mesh, poly, 2);

                Vector3f n0View = transformDirection(modelViewMatrix, n0);
                Vector3f n1View = transformDirection(modelViewMatrix, n1);
                Vector3f n2View = transformDirection(modelViewMatrix, n2);

                rasterizeTriangleLit(
                        p0, p1, p2,
                        p0ndc.z, p1ndc.z, p2ndc.z,
                        v0View, v1View, v2View,
                        n0View, n1View, n2View,
                        pixelWriter, zBuffer, width, height,
                        baseColor
                );
            } else {
                Vector3f v0View = Matrix4f.multiply(modelViewMatrix, v0);
                Vector3f v1View = Matrix4f.multiply(modelViewMatrix, v1);
                Vector3f v2View = Matrix4f.multiply(modelViewMatrix, v2);

                Vector3f n0 = getVertexNormal(mesh, poly, 0);
                Vector3f n1 = getVertexNormal(mesh, poly, 1);
                Vector3f n2 = getVertexNormal(mesh, poly, 2);

                Vector3f n0View = transformDirection(modelViewMatrix, n0);
                Vector3f n1View = transformDirection(modelViewMatrix, n1);
                Vector3f n2View = transformDirection(modelViewMatrix, n2);

                UVTriangle uvTri = getTriangleUV(mesh, poly);

                rasterizeTriangleTexturedLit(
                        p0, p1, p2,
                        p0ndc.z, p1ndc.z, p2ndc.z,
                        v0View, v1View, v2View,
                        n0View, n1View, n2View,
                        uvTri,
                        texture, pixelReader,
                        pixelWriter, zBuffer, width, height
                );
            }

            if (drawWireframe) {
                graphicsContext.strokeLine(p0.x, p0.y, p1.x, p1.y);
                graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
                graphicsContext.strokeLine(p2.x, p2.y, p0.x, p0.y);
            }
        }
    }

    private static void drawCameraIcon(
            final GraphicsContext gc,
            final Camera activeCamera,
            final Camera cameraToDraw,
            final int width,
            final int height) {

        Matrix4f view = activeCamera.getViewMatrix();
        Matrix4f proj = activeCamera.getProjectionMatrix();
        Matrix4f vp = Matrix4f.multiply(proj, view);

        Vector3f pos = cameraToDraw.getPosition();
        Vector3f tgt = cameraToDraw.getTarget();

        Vector3f forward = tgt.subtract(pos).normalize();

        Vector3f worldUp = new Vector3f(0, 1, 0);
        if (Math.abs(forward.dot(worldUp)) > 0.95f) {
            worldUp = new Vector3f(1, 0, 0);
        }

        Vector3f right = Vector3f.cross(worldUp, forward).normalize();
        Vector3f up = Vector3f.cross(forward, right).normalize();

        float len = 6.0f;
        float half = 2.5f;

        Vector3f apex = pos;
        Vector3f baseCenter = pos.add(forward.multiply(len));

        Vector3f c0 = baseCenter.add(right.multiply(-half)).add(up.multiply(-half));
        Vector3f c1 = baseCenter.add(right.multiply( half)).add(up.multiply(-half));
        Vector3f c2 = baseCenter.add(right.multiply( half)).add(up.multiply( half));
        Vector3f c3 = baseCenter.add(right.multiply(-half)).add(up.multiply( half));

        Vector2f a2 = vertexToPoint(multiplyMatrix4ByVector3(vp, apex), width, height);
        Vector2f p0 = vertexToPoint(multiplyMatrix4ByVector3(vp, c0), width, height);
        Vector2f p1 = vertexToPoint(multiplyMatrix4ByVector3(vp, c1), width, height);
        Vector2f p2 = vertexToPoint(multiplyMatrix4ByVector3(vp, c2), width, height);
        Vector2f p3 = vertexToPoint(multiplyMatrix4ByVector3(vp, c3), width, height);

        gc.setStroke(Color.DARKGREEN);

        gc.strokeLine(a2.x, a2.y, p0.x, p0.y);
        gc.strokeLine(a2.x, a2.y, p1.x, p1.y);
        gc.strokeLine(a2.x, a2.y, p2.x, p2.y);
        gc.strokeLine(a2.x, a2.y, p3.x, p3.y);

        gc.strokeLine(p0.x, p0.y, p1.x, p1.y);
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        gc.strokeLine(p2.x, p2.y, p3.x, p3.y);
        gc.strokeLine(p3.x, p3.y, p0.x, p0.y);
    }

    private static void rasterizeTriangle(
            final Vector2f p0, final Vector2f p1, final Vector2f p2,
            final float z0, final float z1, final float z2,
            final PixelWriter pw,
            final float[] zBuffer,
            final int width, final int height,
            final Color color) {

        int minX = clamp((int) Math.floor(min3(p0.x, p1.x, p2.x)), 0, width - 1);
        int maxX = clamp((int) Math.ceil (max3(p0.x, p1.x, p2.x)), 0, width - 1);
        int minY = clamp((int) Math.floor(min3(p0.y, p1.y, p2.y)), 0, height - 1);
        int maxY = clamp((int) Math.ceil (max3(p0.y, p1.y, p2.y)), 0, height - 1);

        float area = edge(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);

        if (Math.abs(area) < 1e-5f) return;

        for (int y = minY; y <= maxY; y++) {
            float py = y + 0.5f;
            for (int x = minX; x <= maxX; x++) {
                float px = x + 0.5f;

                float w0 = edge(p1.x, p1.y, p2.x, p2.y, px, py);
                float w1 = edge(p2.x, p2.y, p0.x, p0.y, px, py);
                float w2 = edge(p0.x, p0.y, p1.x, p1.y, px, py);

                if (!sameSign(w0, area) || !sameSign(w1, area) || !sameSign(w2, area)) continue;

                float a = w0 / area;
                float b = w1 / area;
                float c = w2 / area;

                float z = a * z0 + b * z1 + c * z2;

                int idx = y * width + x;

                zBuffer[idx] = z;
                pw.setColor(x, y, color);
            }
        }
    }

    private static void rasterizeTriangleTextured(
            final Vector2f p0, final Vector2f p1, final Vector2f p2,
            final float z0, final float z1, final float z2,
            final UVTriangle uvTri,
            final Image texture,
            final PixelReader pixelReader,
            final PixelWriter pw,
            final float[] zBuffer,
            final int width, final int height,
            final Color fallbackColor) {

        int minX = clamp((int) Math.floor(min3(p0.x, p1.x, p2.x)), 0, width - 1);
        int maxX = clamp((int) Math.ceil(max3(p0.x, p1.x, p2.x)), 0, width - 1);
        int minY = clamp((int) Math.floor(min3(p0.y, p1.y, p2.y)), 0, height - 1);
        int maxY = clamp((int) Math.ceil(max3(p0.y, p1.y, p2.y)), 0, height - 1);

        float area = edge(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
        if (Math.abs(area) < 1e-5f) return;

        for (int y = minY; y <= maxY; y++) {
            float py = y + 0.5f;
            for (int x = minX; x <= maxX; x++) {
                float px = x + 0.5f;

                float w0 = edge(p1.x, p1.y, p2.x, p2.y, px, py);
                float w1 = edge(p2.x, p2.y, p0.x, p0.y, px, py);
                float w2 = edge(p0.x, p0.y, p1.x, p1.y, px, py);

                if (!sameSign(w0, area) || !sameSign(w1, area) || !sameSign(w2, area)) continue;

                float a = w0 / area;
                float b = w1 / area;
                float c = w2 / area;

                float z = a * z0 + b * z1 + c * z2;

                int idx = y * width + x;
                if (z >= zBuffer[idx]) continue;

                zBuffer[idx] = z;

                Color out = fallbackColor;
                if (uvTri.hasUV && texture != null && pixelReader != null) {
                    float u = a * uvTri.uv0.x + b * uvTri.uv1.x + c * uvTri.uv2.x;
                    float v = a * uvTri.uv0.y + b * uvTri.uv1.y + c * uvTri.uv2.y;
                    out = sampleTextureNearest(texture, pixelReader, u, v);
                }
                pw.setColor(x, y, out);
            }
        }
    }

    private static void rasterizeTriangleLit(
            final Vector2f p0, final Vector2f p1, final Vector2f p2,
            final float z0, final float z1, final float z2,
            final Vector3f v0View, final Vector3f v1View, final Vector3f v2View,
            final Vector3f n0View, final Vector3f n1View, final Vector3f n2View,
            final PixelWriter pw,
            final float[] zBuffer,
            final int width, final int height,
            final Color baseColor) {

        int minX = clamp((int) Math.floor(min3(p0.x, p1.x, p2.x)), 0, width - 1);
        int maxX = clamp((int) Math.ceil(max3(p0.x, p1.x, p2.x)), 0, width - 1);
        int minY = clamp((int) Math.floor(min3(p0.y, p1.y, p2.y)), 0, height - 1);
        int maxY = clamp((int) Math.ceil(max3(p0.y, p1.y, p2.y)), 0, height - 1);

        float area = edge(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
        if (Math.abs(area) < 1e-5f) return;

        final float ambient = 0.2f;
        final float diffuseK = 0.8f;

        for (int y = minY; y <= maxY; y++) {
            float py = y + 0.5f;
            for (int x = minX; x <= maxX; x++) {
                float px = x + 0.5f;

                float w0 = edge(p1.x, p1.y, p2.x, p2.y, px, py);
                float w1 = edge(p2.x, p2.y, p0.x, p0.y, px, py);
                float w2 = edge(p0.x, p0.y, p1.x, p1.y, px, py);

                if (!sameSign(w0, area) || !sameSign(w1, area) || !sameSign(w2, area)) continue;

                float a = w0 / area;
                float b = w1 / area;
                float c = w2 / area;

                float z = a * z0 + b * z1 + c * z2;

                int idx = y * width + x;
                if (z >= zBuffer[idx]) continue;

                zBuffer[idx] = z;

                Vector3f n = new Vector3f(
                        a * n0View.x + b * n1View.x + c * n2View.x,
                        a * n0View.y + b * n1View.y + c * n2View.y,
                        a * n0View.z + b * n1View.z + c * n2View.z
                ).normalize();

                Vector3f pos = new Vector3f(
                        a * v0View.x + b * v1View.x + c * v2View.x,
                        a * v0View.y + b * v1View.y + c * v2View.y,
                        a * v0View.z + b * v1View.z + c * v2View.z
                );

                Vector3f L = new Vector3f(-pos.x, -pos.y, -pos.z).normalize();

                float diff = Math.max(0.0f, n.dot(L));
                float intensity = clamp01(ambient + diffuseK * diff);

                Color out = new Color(
                        clamp01((float) baseColor.getRed() * intensity),
                        clamp01((float) baseColor.getGreen() * intensity),
                        clamp01((float) baseColor.getBlue() * intensity),
                        1.0
                );

                pw.setColor(x, y, out);
            }
        }
    }

    private static void rasterizeTriangleTexturedLit(
            final Vector2f p0, final Vector2f p1, final Vector2f p2,
            final float z0, final float z1, final float z2,
            final Vector3f v0View, final Vector3f v1View, final Vector3f v2View,
            final Vector3f n0View, final Vector3f n1View, final Vector3f n2View,
            final UVTriangle uvTri,
            final Image texture,
            final PixelReader pixelReader,
            final PixelWriter pw,
            final float[] zBuffer,
            final int width,
            final int height) {

        int minX = clamp((int) Math.floor(min3(p0.x, p1.x, p2.x)), 0, width - 1);
        int maxX = clamp((int) Math.ceil(max3(p0.x, p1.x, p2.x)), 0, width - 1);
        int minY = clamp((int) Math.floor(min3(p0.y, p1.y, p2.y)), 0, height - 1);
        int maxY = clamp((int) Math.ceil(max3(p0.y, p1.y, p2.y)), 0, height - 1);

        float area = edge(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
        if (Math.abs(area) < 1e-5f) return;

        final float ambient = 0.2f;
        final float diffuseK = 0.8f;

        for (int y = minY; y <= maxY; y++) {
            float py = y + 0.5f;
            for (int x = minX; x <= maxX; x++) {
                float px = x + 0.5f;

                float w0 = edge(p1.x, p1.y, p2.x, p2.y, px, py);
                float w1 = edge(p2.x, p2.y, p0.x, p0.y, px, py);
                float w2 = edge(p0.x, p0.y, p1.x, p1.y, px, py);

                if (!sameSign(w0, area) || !sameSign(w1, area) || !sameSign(w2, area)) continue;

                float a = w0 / area;
                float b = w1 / area;
                float c = w2 / area;

                float z = a * z0 + b * z1 + c * z2;

                int idx = y * width + x;
                if (z >= zBuffer[idx]) continue;

                zBuffer[idx] = z;

                Color baseColor = Color.LIGHTGRAY;
                if (uvTri.hasUV && texture != null && pixelReader != null) {
                    float u = a * uvTri.uv0.x + b * uvTri.uv1.x + c * uvTri.uv2.x;
                    float v = a * uvTri.uv0.y + b * uvTri.uv1.y + c * uvTri.uv2.y;
                    baseColor = sampleTextureNearest(texture, pixelReader, u, v);
                }

                Vector3f n = new Vector3f(
                        a * n0View.x + b * n1View.x + c * n2View.x,
                        a * n0View.y + b * n1View.y + c * n2View.y,
                        a * n0View.z + b * n1View.z + c * n2View.z
                ).normalize();

                Vector3f pos = new Vector3f(
                        a * v0View.x + b * v1View.x + c * v2View.x,
                        a * v0View.y + b * v1View.y + c * v2View.y,
                        a * v0View.z + b * v1View.z + c * v2View.z
                );

                Vector3f L = new Vector3f(-pos.x, -pos.y, -pos.z).normalize();

                float diff = Math.max(0.0f, n.dot(L));
                float intensity = clamp01(ambient + diffuseK * diff);

                Color lit = new Color(
                        clamp01((float) baseColor.getRed() * intensity),
                        clamp01((float) baseColor.getGreen() * intensity),
                        clamp01((float) baseColor.getBlue() * intensity),
                        1.0
                );

                pw.setColor(x, y, lit);
            }
        }
    }

    private static Vector3f getVertexNormal(final Model mesh, final Polygon poly, final int k) {
        ArrayList<Integer> nIdx = poly.getNormalIndices();
        if (nIdx != null && !nIdx.isEmpty() && k < nIdx.size()) {
            int ni = nIdx.get(k);
            if (ni >= 0 && ni < mesh.normals.size()) return mesh.normals.get(ni);
        }
        return new Vector3f(0, 0, 1);
    }

    private static class UVTriangle {
        final boolean hasUV;
        final Vector2f uv0, uv1, uv2;

        UVTriangle(boolean hasUV, Vector2f uv0, Vector2f uv1, Vector2f uv2) {
            this.hasUV = hasUV;
            this.uv0 = uv0;
            this.uv1 = uv1;
            this.uv2 = uv2;
        }
    }

    private static UVTriangle getTriangleUV(final Model mesh, final Polygon poly) {
        ArrayList<Integer> tIdx = poly.getTextureVertexIndices();
        if (tIdx == null || tIdx.size() != 3) {
            return new UVTriangle(false, new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0));
        }
        if (mesh.textureVertices == null || mesh.textureVertices.isEmpty()) {
            return new UVTriangle(false, new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0));
        }

        int t0 = tIdx.get(0), t1 = tIdx.get(1), t2 = tIdx.get(2);
        if (t0 < 0 || t0 >= mesh.textureVertices.size()) return new UVTriangle(false, new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0));
        if (t1 < 0 || t1 >= mesh.textureVertices.size()) return new UVTriangle(false, new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0));
        if (t2 < 0 || t2 >= mesh.textureVertices.size()) return new UVTriangle(false, new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0));

        return new UVTriangle(true,
                mesh.textureVertices.get(t0),
                mesh.textureVertices.get(t1),
                mesh.textureVertices.get(t2));
    }

    private static Vector3f transformDirection(final Matrix4f m, final Vector3f d) {
        float x = m.get(0, 0) * d.x + m.get(0, 1) * d.y + m.get(0, 2) * d.z;
        float y = m.get(1, 0) * d.x + m.get(1, 1) * d.y + m.get(1, 2) * d.z;
        float z = m.get(2, 0) * d.x + m.get(2, 1) * d.y + m.get(2, 2) * d.z;
        return new Vector3f(x, y, z).normalize();
    }

    private static Color sampleTextureNearest(final Image tex, final PixelReader pr, float u, float v) {
        u = wrap01(u);
        v = wrap01(v);
        v = 1.0f - v;

        int w = (int) tex.getWidth();
        int h = (int) tex.getHeight();
        if (w <= 0 || h <= 0) return Color.LIGHTGRAY;

        int x = clamp((int) (u * (w - 1)), 0, w - 1);
        int y = clamp((int) (v * (h - 1)), 0, h - 1);
        return pr.getColor(x, y);
    }

    private static float wrap01(float t) {
        return t - (float) Math.floor(t);
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static float edge(float ax, float ay, float bx, float by, float px, float py) {
        return (px - ax) * (by - ay) - (py - ay) * (bx - ax);
    }

    private static boolean sameSign(float v, float ref) {
        if (ref > 0) return v >= 0;
        return v <= 0;
    }

    private static float min3(float a, float b, float c) {
        return Math.min(a, Math.min(b, c));
    }

    private static float max3(float a, float b, float c) {
        return Math.max(a, Math.max(b, c));
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        return Math.min(v, hi);
    }
    public static void renderCameraIcons(
            final GraphicsContext graphicsContext,
            final Camera activeCamera,
            final ArrayList<Camera> cameras,
            final int activeCameraIndex,
            final int width,
            final int height) {

        if (cameras == null || cameras.isEmpty()) return;

        for (int i = 0; i < cameras.size(); i++) {
            if (i == activeCameraIndex) continue;
            drawCameraIcon(graphicsContext, activeCamera, cameras.get(i), width, height);
        }
    }

}