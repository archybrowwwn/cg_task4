package com.cgvsu.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class Vector3fTest {
    private final float EPS = 1e-7f;
    
    @Test
    void testAdd() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f result = v1.add(v2);

        assertEquals(5, result.x, EPS);
        assertEquals(7, result.y, EPS);
        assertEquals(9, result.z, EPS);

        assertEquals(1, v1.x, EPS);
    }

    @Test
    void testLength() {
        Vector3f v = new Vector3f(3, 4, 0);
        assertEquals(5, v.length(), EPS);
    }

    @Test
    void testNormalize() {
        Vector3f v = new Vector3f(10, 0, 0);
        Vector3f norm = v.normalize();

        assertEquals(1, norm.x, EPS);
        assertEquals(0, norm.y, EPS);
        assertEquals(0, norm.z, EPS);
    }

    @Test
    void testCrossProduct() {
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(0, 1, 0);
        Vector3f result = Vector3f.cross(v1, v2);

        assertEquals(0, result.x, EPS);
        assertEquals(0, result.y, EPS);
        assertEquals(1, result.z, EPS);
    }

    @Test
    void testSubtract() {
        Vector3f v1 = new Vector3f(5, 6, 7);
        Vector3f v2 = new Vector3f(1, 2, 3);
        Vector3f result = v1.subtract(v2);

        assertEquals(4, result.x, EPS);
        assertEquals(4, result.y, EPS);
        assertEquals(4, result.z, EPS);
    }

    @Test
    void testMultiply() {
        Vector3f v = new Vector3f(1, 2, 3);
        Vector3f result = v.multiply(2.5f);

        assertEquals(2.5f, result.x, EPS);
        assertEquals(5.0f, result.y, EPS);
        assertEquals(7.5f, result.z, EPS);
    }

    @Test
    void testDotProduct() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        float result = v1.dot(v2);

        assertEquals(32, result, EPS);
    }
}
