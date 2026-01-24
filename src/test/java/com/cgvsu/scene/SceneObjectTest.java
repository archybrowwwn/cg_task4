package com.cgvsu.scene;

import com.cgvsu.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SceneObjectTest {

    @Test
    public void constructor_setsModelAndName_andDefaultsAreCorrect() {
        Model m = new Model();
        SceneObject obj = new SceneObject(m, "Cube");

        assertSame(m, obj.getModel());
        assertEquals("Cube", obj.getName());

        assertEquals(0.0f, obj.position.x, 1e-6);
        assertEquals(0.0f, obj.position.y, 1e-6);
        assertEquals(0.0f, obj.position.z, 1e-6);

        assertEquals(0.0f, obj.rotationDeg.x, 1e-6);
        assertEquals(0.0f, obj.rotationDeg.y, 1e-6);
        assertEquals(0.0f, obj.rotationDeg.z, 1e-6);

        assertEquals(1.0f, obj.scale.x, 1e-6);
        assertEquals(1.0f, obj.scale.y, 1e-6);
        assertEquals(1.0f, obj.scale.z, 1e-6);
    }
}
