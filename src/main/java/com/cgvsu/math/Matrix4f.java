package com.cgvsu.math;

/**
 * MATH LIBRARY (Артём)
 * -------------------------------------------
 * Реализация матрицы 4x4.
 * Включает методы для создания единичной матрицы,
 * аффинных преобразований (Translation, Rotation, Scale)
 * и умножения матриц. Основа для выполнения 3-го задания.
 */

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

    // [Task 3] Метод для создания единичной матрицы
    public static Matrix4f identity() {
        return new Matrix4f();
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

    // [Task 3] Матрица Перемещения (Translation)
    public static Matrix4f translation(float tx, float ty, float tz) {
        Matrix4f matrix = new Matrix4f();
        matrix.set(0, 3, tx);
        matrix.set(1, 3, ty);
        matrix.set(2, 3, tz);
        return matrix;
    }

    // [Task 3] Матрица Масштабирования (Scale)
    public static Matrix4f scale(float sx, float sy, float sz) {
        Matrix4f matrix = new Matrix4f();
        matrix.set(0, 0, sx);
        matrix.set(1, 1, sy);
        matrix.set(2, 2, sz);
        return matrix;
    }

    // [Task 3] Поворот вокруг оси X
    public static Matrix4f rotateX(float angleRadians) {
        Matrix4f matrix = new Matrix4f();
        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);
        matrix.set(1, 1, cos);
        matrix.set(1, 2, -sin);
        matrix.set(2, 1, sin);
        matrix.set(2, 2, cos);
        return matrix;
    }

    // [Task 3] Поворот вокруг оси Y
    public static Matrix4f rotateY(float angleRadians) {
        Matrix4f matrix = new Matrix4f();
        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);
        matrix.set(0, 0, cos);
        matrix.set(0, 2, sin);
        matrix.set(2, 0, -sin);
        matrix.set(2, 2, cos);
        return matrix;
    }

    // [Task 3] Поворот вокруг оси Z
    public static Matrix4f rotateZ(float angleRadians) {
        Matrix4f matrix = new Matrix4f();
        float cos = (float) Math.cos(angleRadians);
        float sin = (float) Math.sin(angleRadians);
        matrix.set(0, 0, cos);
        matrix.set(0, 1, -sin);
        matrix.set(1, 0, sin);
        matrix.set(1, 1, cos);
        return matrix;
    }
}
