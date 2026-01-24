package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ModelEditorTest {

    private static Polygon poly(int... v) {
        ArrayList<Integer> vi = new ArrayList<>();
        for (int x : v) vi.add(x);
        Polygon p = new Polygon();
        p.setVertexIndices(vi);
        return p;
    }

    @Test
    public void deletePolygon_removesPolygonAndCleansUnusedVertices_reindexesRemainingPolygons() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0,0,0)); // v0
        m.vertices.add(new Vector3f(1,0,0)); // v1
        m.vertices.add(new Vector3f(0,1,0)); // v2
        m.vertices.add(new Vector3f(0,0,1)); // v3

        Polygon p0 = poly(0, 1, 2);
        Polygon p1 = poly(0, 2, 3);
        m.polygons.add(p0);
        m.polygons.add(p1);

        ArrayList<Integer> oldIdx = new ArrayList<>(p1.getVertexIndices());
        ArrayList<Vector3f> expectedVerts = new ArrayList<>();
        for (int i : oldIdx) expectedVerts.add(m.vertices.get(i));

        ModelEditor.deletePolygon(m, 0);

        assertEquals(1, m.polygons.size());

        ArrayList<Integer> newIdx = m.polygons.get(0).getVertexIndices();
        for (int i : newIdx) {
            assertTrue(i >= 0 && i < m.vertices.size(), "Polygon index out of bounds after reindex");
        }

        assertEquals(expectedVerts.size(), m.vertices.size());
        for (Vector3f v : expectedVerts) {
            boolean found = false;
            for (Vector3f vv : m.vertices) {
                if (vv.x == v.x && vv.y == v.y && vv.z == v.z) { found = true; break; }
            }
            assertTrue(found, "Expected vertex not found after cleanup: " + v);
        }
    }


    @Test
    public void deleteVertex_removesPolygonsThatUseIt_andCleansVertices() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0, 0, 0));
        m.vertices.add(new Vector3f(1, 0, 0));
        m.vertices.add(new Vector3f(0, 1, 0));

        m.polygons.add(poly(0, 1, 2));

        ModelEditor.deleteVertex(m, 1);

        assertTrue(m.polygons.isEmpty(), "Polygon using deleted vertex should be removed");
        assertTrue(m.vertices.isEmpty(), "With no polygons, all vertices become unused and should be removed");
    }

    @Test
    public void deletePolygon_invalidArgs_doNothing() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0,0,0));
        m.vertices.add(new Vector3f(1,0,0));
        m.vertices.add(new Vector3f(0,1,0));
        m.polygons.add(poly(0,1,2));

        ModelEditor.deletePolygon(m, -1);
        ModelEditor.deletePolygon(m, 5);

        assertEquals(1, m.polygons.size());
        assertEquals(3, m.vertices.size());
    }
}
