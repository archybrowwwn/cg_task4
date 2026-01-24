package com.cgvsu.ModelPreprocessor;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelPreprocessor;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelPreprocessorTest {

    @SuppressWarnings("unchecked")
    private static <T> T readField(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (NoSuchFieldException e) {
            try {
                Field f = obj.getClass().getField(name);
                return (T) f.get(obj);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot read field: " + name, ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot read field: " + name, e);
        }
    }

    private static void writeField(Object obj, String name, Object value) {
        try {
            Field f;
            try {
                f = obj.getClass().getDeclaredField(name);
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                f = obj.getClass().getField(name);
            }
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write field: " + name, e);
        }
    }

    private static ArrayList<Polygon> polygons(Model m) {
        @SuppressWarnings("unchecked")
        ArrayList<Polygon> v = readField(m, "polygons");
        return v;
    }

    private static ArrayList<Vector3f> vertices(Model m) {
        @SuppressWarnings("unchecked")
        ArrayList<Vector3f> v = readField(m, "vertices");
        return v;
    }

    private static ArrayList<Vector3f> normals(Model m) {
        @SuppressWarnings("unchecked")
        ArrayList<Vector3f> v = readField(m, "normals");
        return v;
    }

    private static Model newModelWithLists() {
        Model m = new Model();
        if (readField(m, "vertices") == null) writeField(m, "vertices", new ArrayList<Vector3f>());
        if (readField(m, "polygons") == null) writeField(m, "polygons", new ArrayList<Polygon>());
        if (readField(m, "normals") == null) writeField(m, "normals", new ArrayList<Vector3f>());
        return m;
    }

    private static Polygon poly(List<Integer> v, List<Integer> t, List<Integer> n) {
        Polygon p = new Polygon();
        p.setVertexIndices(new ArrayList<>(v));
        if (t != null) p.setTextureVertexIndices(new ArrayList<>(t));
        if (n != null) p.setNormalIndices(new ArrayList<>(n));
        return p;
    }

    private static boolean isFinite(float x) {
        return !Float.isNaN(x) && !Float.isInfinite(x);
    }

    @Test
    void triangulate_emptyPolygons_staysEmpty() {
        Model m = newModelWithLists();
        ModelPreprocessor.triangulate(m);
        assertEquals(0, polygons(m).size());
    }

    @Test
    void triangulate_triangleStaysTriangle_andPreservesIndices() {
        Model m = newModelWithLists();
        polygons(m).add(poly(List.of(5, 7, 9), List.of(50, 70, 90), List.of(6, 8, 10)));

        ModelPreprocessor.triangulate(m);

        assertEquals(1, polygons(m).size());
        Polygon tri = polygons(m).get(0);
        assertEquals(List.of(5, 7, 9), tri.getVertexIndices());
        assertEquals(List.of(50, 70, 90), tri.getTextureVertexIndices());
        assertEquals(List.of(6, 8, 10), tri.getNormalIndices());
    }

    @Test
    void triangulate_quadToTwoTriangles_vertexOrderFanFromFirstVertex() {
        Model m = newModelWithLists();
        polygons(m).add(poly(List.of(0, 1, 2, 3), null, null));

        ModelPreprocessor.triangulate(m);

        assertEquals(2, polygons(m).size());
        assertEquals(List.of(0, 1, 2), polygons(m).get(0).getVertexIndices());
        assertEquals(List.of(0, 2, 3), polygons(m).get(1).getVertexIndices());
    }

    @Test
    void triangulate_pentagonToThreeTriangles_withTextureAndNormals() {
        Model m = newModelWithLists();
        polygons(m).add(poly(
                List.of(10, 11, 12, 13, 14),
                List.of(20, 21, 22, 23, 24),
                List.of(30, 31, 32, 33, 34)
        ));

        ModelPreprocessor.triangulate(m);

        assertEquals(3, polygons(m).size());

        assertEquals(List.of(10, 11, 12), polygons(m).get(0).getVertexIndices());
        assertEquals(List.of(10, 12, 13), polygons(m).get(1).getVertexIndices());
        assertEquals(List.of(10, 13, 14), polygons(m).get(2).getVertexIndices());

        assertEquals(List.of(20, 21, 22), polygons(m).get(0).getTextureVertexIndices());
        assertEquals(List.of(20, 22, 23), polygons(m).get(1).getTextureVertexIndices());
        assertEquals(List.of(20, 23, 24), polygons(m).get(2).getTextureVertexIndices());

        assertEquals(List.of(30, 31, 32), polygons(m).get(0).getNormalIndices());
        assertEquals(List.of(30, 32, 33), polygons(m).get(1).getNormalIndices());
        assertEquals(List.of(30, 33, 34), polygons(m).get(2).getNormalIndices());
    }

    @Test
    void triangulate_doesNotSetTextureIndices_whenTextureListIsNullOrEmpty() {
        Model m1 = newModelWithLists();
        polygons(m1).add(poly(List.of(0, 1, 2, 3), null, List.of(0, 1, 2, 3)));
        ModelPreprocessor.triangulate(m1);
        for (Polygon tri : polygons(m1)) {
            assertTrue(tri.getTextureVertexIndices() == null || tri.getTextureVertexIndices().isEmpty());
        }

        Model m2 = newModelWithLists();
        polygons(m2).add(poly(List.of(0, 1, 2, 3), List.of(), List.of(0, 1, 2, 3)));
        ModelPreprocessor.triangulate(m2);
        for (Polygon tri : polygons(m2)) {
            assertTrue(tri.getTextureVertexIndices() == null || tri.getTextureVertexIndices().isEmpty());
        }
    }

    @Test
    void triangulate_doesNotSetNormalIndices_whenNormalListIsNullOrEmpty() {
        Model m1 = newModelWithLists();
        polygons(m1).add(poly(List.of(0, 1, 2, 3), List.of(0, 1, 2, 3), null));
        ModelPreprocessor.triangulate(m1);
        for (Polygon tri : polygons(m1)) {
            assertTrue(tri.getNormalIndices() == null || tri.getNormalIndices().isEmpty());
        }

        Model m2 = newModelWithLists();
        polygons(m2).add(poly(List.of(0, 1, 2, 3), List.of(0, 1, 2, 3), List.of()));
        ModelPreprocessor.triangulate(m2);
        for (Polygon tri : polygons(m2)) {
            assertTrue(tri.getNormalIndices() == null || tri.getNormalIndices().isEmpty());
        }
    }

    @Test
    void triangulate_replacesPolygonListWithNewList() {
        Model m = newModelWithLists();
        polygons(m).add(poly(List.of(0, 1, 2, 3), null, null));
        ArrayList<Polygon> oldRef = polygons(m);

        ModelPreprocessor.triangulate(m);

        ArrayList<Polygon> newRef = polygons(m);
        assertNotSame(oldRef, newRef);
        assertEquals(2, newRef.size());
    }

    @Test
    void recalculateNormals_createsNormalsPerVertex_andSetsNormalIndicesEqualToVertexIndices() {
        Model m = newModelWithLists();
        vertices(m).add(new Vector3f(0, 0, 0));
        vertices(m).add(new Vector3f(1, 0, 0));
        vertices(m).add(new Vector3f(0, 1, 0));
        polygons(m).add(poly(List.of(0, 1, 2), null, null));

        ModelPreprocessor.recalculateNormals(m);

        assertEquals(3, normals(m).size());
        for (Vector3f n : normals(m)) {
            float len = (float) Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
            assertTrue(isFinite(n.x) && isFinite(n.y) && isFinite(n.z));
            assertTrue(Math.abs(len - 1.0f) < 1e-4);
        }
        assertEquals(polygons(m).get(0).getVertexIndices(), polygons(m).get(0).getNormalIndices());
    }

    @Test
    void recalculateNormals_skipsNonTriangles_doesNotCrash() {
        Model m = newModelWithLists();
        vertices(m).add(new Vector3f(0, 0, 0));
        vertices(m).add(new Vector3f(1, 0, 0));
        vertices(m).add(new Vector3f(0, 1, 0));
        vertices(m).add(new Vector3f(0, 0, 1));
        polygons(m).add(poly(List.of(0, 1, 2, 3), null, null));
        polygons(m).add(poly(List.of(0, 1, 2), null, null));

        assertDoesNotThrow(() -> ModelPreprocessor.recalculateNormals(m));

        assertEquals(4, normals(m).size());
        for (Polygon p : polygons(m)) {
            assertEquals(p.getVertexIndices(), p.getNormalIndices());
        }
    }


    @Test
    void recalculateNormals_clearsOldNormals() {
        Model m = newModelWithLists();
        vertices(m).add(new Vector3f(0, 0, 0));
        vertices(m).add(new Vector3f(1, 0, 0));
        vertices(m).add(new Vector3f(0, 1, 0));
        polygons(m).add(poly(List.of(0, 1, 2), null, null));
        normals(m).add(new Vector3f(123, 456, 789));

        ModelPreprocessor.recalculateNormals(m);

        assertEquals(3, normals(m).size());
    }
}
