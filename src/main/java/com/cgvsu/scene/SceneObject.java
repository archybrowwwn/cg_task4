package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;

public class SceneObject {
    private final Model model;
    private final String name;

    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f rotationDeg = new Vector3f(0, 0, 0);
    public Vector3f scale = new Vector3f(1, 1, 1);

    public SceneObject(Model model, String name) {
        this.model = model;
        this.name = name;
    }

    public Model getModel() { return model; }
    public String getName() { return name; }
}
