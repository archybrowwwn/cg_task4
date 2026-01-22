package com.cgvsu.math;

import java.util.Arrays;
public class Matrix4f {

    private final float[] m;
    public static final float EPS = 1e-7f;

    public Matrix4f() {
        m = new float[16];
        m[0] = 1;
        m[5] = 1;
        m[10] = 1;
        m[15] = 1;
    }

    public Matrix4f(float[] m) {
        this.m = m;
    }

    public float get(int row, int col) {
        return m[row * 4 + col];
    }

    public void set(int row, int col, float value) {
        m[row * 4 + col] = value;
    }

    public static Vector3f multiply(Matrix4f matrix, Vector3f vertex) {
        float x = vertex.x;
        float y = vertex.y;
        float z = vertex.z;
        float w = 1.0f;

        float resX = matrix.get(0, 0) * x + matrix.get(0, 1) * y + matrix.get(0, 2) * z + matrix.get(0, 3) * w;
        float resY = matrix.get(1, 0) * x + matrix.get(1, 1) * y + matrix.get(1, 2) * z + matrix.get(1, 3) * w;
        float resZ = matrix.get(2, 0) * x + matrix.get(2, 1) * y + matrix.get(2, 2) * z + matrix.get(2, 3) * w;
        float resW = matrix.get(3, 0) * x + matrix.get(3, 1) * y + matrix.get(3, 2) * z + matrix.get(3, 3) * w;

        if (Math.abs(resW) > EPS) {
            resX /= resW;
            resY /= resW;
            resZ /= resW;
        }

        return new Vector3f(resX, resY, resZ);
    }

    public static Matrix4f multiply(Matrix4f m1, Matrix4f m2) {
        Matrix4f result = new Matrix4f();
        Arrays.fill(result.m, 0);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                float sum = 0;
                for (int i = 0; i < 4; i++) {
                    sum += m1.get(row, i) * m2.get(i, col);
                }
                result.set(row, col, sum);
            }
        }
        return result;
    }
}
