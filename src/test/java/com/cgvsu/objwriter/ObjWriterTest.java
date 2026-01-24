package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ObjWriterTest {

    private static Polygon poly(int... v) {
        ArrayList<Integer> vi = new ArrayList<>();
        for (int x : v) vi.add(x);
        Polygon p = new Polygon();
        p.setVertexIndices(vi);
        return p;
    }

    @Test
    public void write_minimalModel_writesVerticesAndFaces() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0, 0, 0));
        m.vertices.add(new Vector3f(1, 0, 0));
        m.vertices.add(new Vector3f(0, 1, 0));
        m.polygons.add(poly(0, 1, 2));

        String obj = ObjWriter.write(m);

        assertTrue(obj.contains("\nv 0.0 0.0 0.0\n") || obj.startsWith("v 0.0 0.0 0.0\n"),
                "OBJ should contain vertex records");
        assertTrue(obj.contains("f 1 2 3\n"), "OBJ should contain face record with 1-based indices");
    }

    @Test
    public void write_withTextureAndNormals_writesVtVnAndFullFaceTriplets() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0, 0, 0));
        m.vertices.add(new Vector3f(1, 0, 0));
        m.vertices.add(new Vector3f(0, 1, 0));

        m.textureVertices.add(new Vector2f(0, 0));
        m.textureVertices.add(new Vector2f(1, 0));
        m.textureVertices.add(new Vector2f(0, 1));

        m.normals.add(new Vector3f(0, 0, 1));
        m.normals.add(new Vector3f(0, 0, 1));
        m.normals.add(new Vector3f(0, 0, 1));

        Polygon p = poly(0, 1, 2);

        ArrayList<Integer> vt = new ArrayList<>();
        vt.add(0); vt.add(1); vt.add(2);
        p.setTextureVertexIndices(vt);

        ArrayList<Integer> vn = new ArrayList<>();
        vn.add(0); vn.add(1); vn.add(2);
        p.setNormalIndices(vn);

        m.polygons.add(p);

        String obj = ObjWriter.write(m);

        assertTrue(obj.contains("vt 0.0 0.0\n"), "OBJ should contain texture vertices");
        assertTrue(obj.contains("vn 0.0 0.0 1.0\n"), "OBJ should contain normals");
        assertTrue(obj.contains("f 1/1/1 2/2/2 3/3/3\n"), "OBJ should contain triplets v/vt/vn");
    }

    @Test
    public void write_invalidVertexIndex_throwsObjWriterException() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0, 0, 0));
        m.vertices.add(new Vector3f(1, 0, 0));
        m.vertices.add(new Vector3f(0, 1, 0));
        m.polygons.add(poly(0, 1, 5));

        assertThrows(ObjWriterException.class, () -> ObjWriter.write(m));
    }

    @Test
    public void write_inconsistentTextureIndicesCount_throwsObjWriterException() {
        Model m = new Model();
        m.vertices.add(new Vector3f(0, 0, 0));
        m.vertices.add(new Vector3f(1, 0, 0));
        m.vertices.add(new Vector3f(0, 1, 0));
        m.textureVertices.add(new Vector2f(0, 0));

        Polygon p = poly(0, 1, 2);
        ArrayList<Integer> vt = new ArrayList<>();
        vt.add(0);
        p.setTextureVertexIndices(vt);
        m.polygons.add(p);

        assertThrows(ObjWriterException.class, () -> ObjWriter.write(m));
    }
}
