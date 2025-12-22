package com.cgvsu.math;

public class Vector3f {
    public float x, y, z;
    public static final float EPS = 1e-7f;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Vector3f other) {
        return Math.abs(x - other.x) < EPS &&
               Math.abs(y - other.y) < EPS &&
               Math.abs(z - other.z) < EPS;
    }

    // Сложение векторов
    public void add(Vector3f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
    }

    // Вычитание векторов (возвращает новый вектор)
    public static Vector3f subtract(Vector3f v1, Vector3f v2) {
        return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    // Векторное произведение (для вычисления перпендикуляра к поверхности)
    public static Vector3f cross(Vector3f v1, Vector3f v2) {
        return new Vector3f(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x
        );
    }

    // Длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    // Нормализация вектора (приведение к длине 1)
    public void normalize() {
        float len = length();
        if (len > EPS) {
            this.x /= len;
            this.y /= len;
            this.z /= len;
        }
    }
}
