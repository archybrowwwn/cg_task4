package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;

import java.util.ArrayList;
import java.util.Arrays;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        final float[] zBuffer = new float[width * height];
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();
        final Color fillColor = Color.LIGHTGRAY;

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            Polygon poly = mesh.polygons.get(polygonInd);
            ArrayList<Integer> vIdx = poly.getVertexIndices();

            if (vIdx.size() != 3) {
                continue;
            }

            Vector3f v0 = mesh.vertices.get(vIdx.get(0));
            Vector3f v1 = mesh.vertices.get(vIdx.get(1));
            Vector3f v2 = mesh.vertices.get(vIdx.get(2));

            javax.vecmath.Vector3f p0ndc = multiplyMatrix4ByVector3(mvp, new javax.vecmath.Vector3f(v0.x, v0.y, v0.z));
            javax.vecmath.Vector3f p1ndc = multiplyMatrix4ByVector3(mvp, new javax.vecmath.Vector3f(v1.x, v1.y, v1.z));
            javax.vecmath.Vector3f p2ndc = multiplyMatrix4ByVector3(mvp, new javax.vecmath.Vector3f(v2.x, v2.y, v2.z));

            Point2f p0 = vertexToPoint(p0ndc, width, height);
            Point2f p1 = vertexToPoint(p1ndc, width, height);
            Point2f p2 = vertexToPoint(p2ndc, width, height);

            rasterizeTriangle(
                    p0, p1, p2,
                    p0ndc.z, p1ndc.z, p2ndc.z,
                    pixelWriter, zBuffer, width, height,
                    fillColor
            );
        }
    }

    private static void rasterizeTriangle(
            final Point2f p0, final Point2f p1, final Point2f p2,
            final float z0, final float z1, final float z2,
            final PixelWriter pw,
            final float[] zBuffer,
            final int width, final int height,
            final Color color)
    {
        int minX = clamp((int) Math.floor(min3(p0.x, p1.x, p2.x)), 0, width - 1);
        int maxX = clamp((int) Math.ceil (max3(p0.x, p1.x, p2.x)), 0, width - 1);
        int minY = clamp((int) Math.floor(min3(p0.y, p1.y, p2.y)), 0, height - 1);
        int maxY = clamp((int) Math.ceil (max3(p0.y, p1.y, p2.y)), 0, height - 1);

        float area = edge(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
        if (area == 0.0f) return;

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
                if (z < zBuffer[idx]) {
                    zBuffer[idx] = z;
                    pw.setColor(x, y, color);
                }
            }
        }
    }

    private static float edge(float ax, float ay, float bx, float by, float px, float py) {
        return (px - ax) * (by - ay) - (py - ay) * (bx - ax);}

    private static boolean sameSign(float v, float ref) {return (ref > 0) ? (v >= 0) : (v <= 0);}

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
}