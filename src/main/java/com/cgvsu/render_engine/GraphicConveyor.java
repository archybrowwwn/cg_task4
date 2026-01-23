package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMatrix = Matrix4f.scale(scale.x, scale.y, scale.z);
        // добавиль перевод в радианы, что бы модель нормально вращалась
        float rx = (float) Math.toRadians(rotation.x);
        float ry = (float) Math.toRadians(rotation.y);
        float rz = (float) Math.toRadians(rotation.z);

        Matrix4f rotateXMatrix = Matrix4f.rotateX(rx);
        Matrix4f rotateYMatrix = Matrix4f.rotateY(ry);
        Matrix4f rotateZMatrix = Matrix4f.rotateZ(rz);

        Matrix4f rotationMatrix = Matrix4f.multiply(rotateZMatrix, Matrix4f.multiply(rotateYMatrix, rotateXMatrix));

        Matrix4f translationMatrix = Matrix4f.translation(translation.x, translation.y, translation.z);

        return Matrix4f.multiply(translationMatrix, Matrix4f.multiply(rotationMatrix, scaleMatrix));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = eye.subtract(target);
        Vector3f resultX = Vector3f.cross(up, resultZ);
        Vector3f resultY = Vector3f.cross(resultZ, resultX);
        
        resultZ = resultZ.normalize();
        resultX = resultX.normalize();
        resultY = resultY.normalize();

        float[] matrix = new float[]{
                resultX.x, resultX.y, resultX.z, -resultX.dot(eye),
                resultY.x, resultY.y, resultY.z, -resultY.dot(eye),
                resultZ.x, resultZ.y, resultZ.z, -resultZ.dot(eye),
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();

        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));

        result.set(0, 0, tangentMinusOnDegree / aspectRatio);
        result.set(1, 1, tangentMinusOnDegree);
        result.set(2, 2, (farPlane + nearPlane) / (nearPlane - farPlane));
        result.set(2, 3, 2 * (nearPlane * farPlane) / (nearPlane - farPlane));
        result.set(3, 2, -1.0F);
        result.set(3, 3, 0);
        
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        return Matrix4f.multiply(matrix, vertex);
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Vector2f(
            vertex.x * width / 2.0F + width / 2.0F,
            -vertex.y * height / 2.0F + height / 2.0F);
    }

}
