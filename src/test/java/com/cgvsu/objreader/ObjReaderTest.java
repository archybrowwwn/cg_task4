package com.cgvsu.objreader;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;

class ObjReaderTest {
    private static final float EPS = 1e-5f;

    @Test
    public void testParseVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);

        Assertions.assertEquals(1.01f, result.x, EPS);
        Assertions.assertEquals(1.02f, result.y, EPS);
        Assertions.assertEquals(1.03f, result.z, EPS);
    }

    @Test
    public void testParseTextureVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("0.55", "0.66"));
        Vector2f result = ObjReader.parseTextureVertex(wordsInLineWithoutToken, 5);

        Assertions.assertEquals(0.55f, result.x, EPS);
        Assertions.assertEquals(0.66f, result.y, EPS);
    }

    @Test
    public void testParseFace01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1/1/1", "2/2/2", "3/3/3"));

        Polygon result = ObjReader.parseFace(wordsInLineWithoutToken, 5, 3, 3, 3);

        Assertions.assertEquals(0, result.getVertexIndices().get(0));
        Assertions.assertEquals(1, result.getVertexIndices().get(1));
        Assertions.assertEquals(2, result.getVertexIndices().get(2));

        Assertions.assertEquals(0, result.getTextureVertexIndices().get(0));
        Assertions.assertEquals(1, result.getTextureVertexIndices().get(1));
        Assertions.assertEquals(2, result.getTextureVertexIndices().get(2));

        Assertions.assertEquals(0, result.getNormalIndices().get(0));
        Assertions.assertEquals(1, result.getNormalIndices().get(1));
        Assertions.assertEquals(2, result.getNormalIndices().get(2));
    }

    @Test
    public void testParseVertex02() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        ObjReaderException exception = Assertions.assertThrows(ObjReaderException.class, () -> {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        });
        Assertions.assertTrue(exception.getMessage().contains("Failed to parse float value"));
    }

    @Test
    public void testParseVertex03() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        ObjReaderException exception = Assertions.assertThrows(ObjReaderException.class, () -> {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        });
        Assertions.assertTrue(exception.getMessage().contains("Too few vertex arguments"));
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0", "5.0"));
        ObjReaderException exception = Assertions.assertThrows(ObjReaderException.class, () -> {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        });
        Assertions.assertTrue(exception.getMessage().contains("Too many vertex arguments"));
    }
}
