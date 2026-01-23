package com.cgvsu.render_engine;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

public class Camera {

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    // [Артём] Умное перемещение
    public void movePosition(final Vector3f translation) {
        Vector3f forward = Vector3f.subtract(target, position);

        if (forward.length() < 1e-5) return;
        forward = forward.normalize();

        Vector3f up = new Vector3f(0, 1, 0);

        Vector3f right = Vector3f.cross(up, forward).normalize();

        Vector3f moveX = right.multiply(translation.x);
        Vector3f moveY = new Vector3f(0, translation.y, 0);
        Vector3f moveZ = forward.multiply(translation.z);

        Vector3f move = moveX.add(moveY).add(moveZ);

        this.position = this.position.add(move);
        this.target = this.target.add(move);
    }

    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
}
