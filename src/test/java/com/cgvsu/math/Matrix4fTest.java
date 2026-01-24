package com.cgvsu.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Matrix4fTest {
    private final float EPS = 1e-5f;

    @Test
    void testIdentityMatrix() {
        Matrix4f m = new Matrix4f();
        Assertions.assertEquals(1, m.get(0, 0));
        Assertions.assertEquals(1, m.get(1, 1));
        Assertions.assertEquals(1, m.get(2, 2));
        Assertions.assertEquals(1, m.get(3, 3));
        Assertions.assertEquals(0, m.get(0, 1));
        Assertions.assertEquals(0, m.get(2, 3));
    }

    @Test
    void testMultiplyMatrixByVector() {
        Matrix4f m = new Matrix4f();
        m.set(0, 3, 10);
        m.set(1, 3, 20);
        m.set(2, 3, 30);
        
        Vector3f v = new Vector3f(1, 2, 3);
        
        Vector3f result = Matrix4f.multiply(m, v);
        
        Assertions.assertEquals(11, result.x, EPS);
        Assertions.assertEquals(22, result.y, EPS);
        Assertions.assertEquals(33, result.z, EPS);
    }

    @Test
    void testMultiplyMatrixByMatrix() {
        Matrix4f m1 = new Matrix4f();
        m1.set(0, 3, 5);
        
        Matrix4f m2 = new Matrix4f();
        m2.set(1, 3, 10);
        
        Matrix4f result = Matrix4f.multiply(m1, m2);
        
        Assertions.assertEquals(1, result.get(0, 0));
        Assertions.assertEquals(5, result.get(0, 3));
        Assertions.assertEquals(10, result.get(1, 3));
        Assertions.assertEquals(1, result.get(3, 3));
    }
    
    @Test
    void testZeroVectorMultiplication() {
        Matrix4f m = new Matrix4f();
        m.set(0, 3, 100);
        Vector3f v = new Vector3f(0, 0, 0);
        Vector3f result = Matrix4f.multiply(m, v);
        Assertions.assertEquals(100, result.x, EPS);
    }

    @Test
    void testTranslation() {
        Vector3f v = new Vector3f(1, 1, 1);
        Matrix4f t = Matrix4f.translation(5, -2, 0);
        Vector3f res = Matrix4f.multiply(t, v);
        
        Assertions.assertEquals(6, res.x, EPS);
        Assertions.assertEquals(-1, res.y, EPS);
        Assertions.assertEquals(1, res.z, EPS);
    }
    
    @Test
    void testScale() {
        Vector3f v = new Vector3f(1, 1, 1);
        Matrix4f s = Matrix4f.scale(2, 3, 4);
        Vector3f res = Matrix4f.multiply(s, v);
        
        Assertions.assertEquals(2, res.x, EPS);
        Assertions.assertEquals(3, res.y, EPS);
        Assertions.assertEquals(4, res.z, EPS);
    }
    
    @Test
    void testRotationZ() {
        Vector3f v = new Vector3f(1, 0, 0);
        Matrix4f r = Matrix4f.rotateZ((float) (Math.PI / 2));
        Vector3f res = Matrix4f.multiply(r, v);
        
        Assertions.assertEquals(0, res.x, EPS);
        Assertions.assertEquals(1, res.y, EPS);
        Assertions.assertEquals(0, res.z, EPS);
    }

    @Test
    void testRotationX() {
        Vector3f v = new Vector3f(0, 1, 0);

        Matrix4f r = Matrix4f.rotateX((float) (Math.PI / 2));
        Vector3f res = Matrix4f.multiply(r, v);

        Assertions.assertEquals(0, res.x, EPS);
        Assertions.assertEquals(0, res.y, EPS);
        Assertions.assertEquals(1, res.z, EPS);
    }

    @Test
    void testRotationY() {
        Vector3f v = new Vector3f(1, 0, 0);

        Matrix4f r = Matrix4f.rotateY((float) (Math.PI / 2));
        Vector3f res = Matrix4f.multiply(r, v);

        Assertions.assertEquals(0, res.x, EPS);
        Assertions.assertEquals(0, res.y, EPS);
        Assertions.assertEquals(-1, res.z, EPS);
    }
}