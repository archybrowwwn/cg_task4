package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static com.cgvsu.objreader.ObjReader.DeferredFace.faceHasNegativeIndex;

public class ObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        Model result = new Model();

        final ArrayList<DeferredFace> deferredFaces = new ArrayList<>();

        int lineInd = 0;
        Scanner scanner = new Scanner(fileContent);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            ++lineInd;

            int commentStart = line.indexOf('#');
            if (commentStart >= 0) {
                line = line.substring(0, commentStart);
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.get(0);
            wordsInLine.remove(0);

            switch (token) {

                case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
                case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
                case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));

                case OBJ_FACE_TOKEN -> {
                    deferredFaces.add(new DeferredFace(
                            lineInd,
                            new ArrayList<>(wordsInLine),
                            result.vertices.size(),
                            result.textureVertices.size(),
                            result.normals.size()
                    ));
                }
                default -> {
                }
            }
        }

        final int finalV = result.vertices.size();
        final int finalVT = result.textureVertices.size();
        final int finalVN = result.normals.size();

        for (DeferredFace df : deferredFaces) {
            final boolean hasNeg = faceHasNegativeIndex(df.words);

            int vCount = hasNeg ? df.vCountAtTime : finalV;
            int vtCount = hasNeg ? df.vtCountAtTime : finalVT;
            int vnCount = hasNeg ? df.vnCountAtTime : finalVN;

            if (vCount == 0) vCount = finalV;
            if (vtCount == 0) vtCount = finalVT;
            if (vnCount == 0) vnCount = finalVN;

            result.polygons.add(parseFace(df.words, df.lineInd, vCount, vtCount, vnCount));
        }



        return result;
    }

    protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {

        if (wordsInLineWithoutToken.size() > 4) {
            throw new ObjReaderException("Too many vertex arguments.", lineInd);
        }

        try {
            if (wordsInLineWithoutToken.size() < 3) {
                throw new ObjReaderException("Too few vertex arguments.", lineInd);
            }

            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few vertex arguments.", lineInd);
        }
    }

    protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() < 2) {
            throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
        }
        if (wordsInLineWithoutToken.size() > 3) {
            throw new ObjReaderException("Too many texture vertex arguments.", lineInd);
        }

        try {
            return new Vector2f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
        }
    }

    protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        if (wordsInLineWithoutToken.size() > 3) {
            throw new ObjReaderException("Too many normal arguments.", lineInd);
        }

        try {
            if (wordsInLineWithoutToken.size() < 3) {
                throw new ObjReaderException("Too few normal arguments.", lineInd);
            }

            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few normal arguments.", lineInd);
        }
    }

    protected static Polygon parseFace(
            final ArrayList<String> wordsInLineWithoutToken,
            int lineInd,
            int verticesCount,
            int textureVerticesCount,
            int normalsCount) {

        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException("Face has less than 3 vertices.", lineInd);
        }

        ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

        boolean anyVT = false;
        boolean anyVN = false;

        for (String s : wordsInLineWithoutToken) {
            FaceFlags flags = parseFaceWord(
                    s,
                    onePolygonVertexIndices,
                    onePolygonTextureVertexIndices,
                    onePolygonNormalIndices,
                    lineInd,
                    verticesCount,
                    textureVerticesCount,
                    normalsCount
            );
            anyVT = anyVT || flags.hasVT;
            anyVN = anyVN || flags.hasVN;
        }

        if (anyVT && onePolygonTextureVertexIndices.size() != onePolygonVertexIndices.size()) {
            throw new ObjReaderException("Inconsistent texture indices in face.", lineInd);
        }
        if (anyVN && onePolygonNormalIndices.size() != onePolygonVertexIndices.size()) {
            throw new ObjReaderException("Inconsistent normal indices in face.", lineInd);
        }

        Polygon result = new Polygon();
        result.setVertexIndices(onePolygonVertexIndices);
        result.setTextureVertexIndices(onePolygonTextureVertexIndices);
        result.setNormalIndices(onePolygonNormalIndices);
        return result;
    }

    protected static class FaceFlags {
        public final boolean hasVT;
        public final boolean hasVN;

        public FaceFlags(boolean hasVT, boolean hasVN) {
            this.hasVT = hasVT;
            this.hasVN = hasVN;
        }
    }

    protected static FaceFlags parseFaceWord(
            String wordInLine,
            ArrayList<Integer> onePolygonVertexIndices,
            ArrayList<Integer> onePolygonTextureVertexIndices,
            ArrayList<Integer> onePolygonNormalIndices,
            int lineInd,
            int verticesCount,
            int textureVerticesCount,
            int normalsCount) {
        try {
            String[] wordIndices = wordInLine.split("/", -1);

            if (wordIndices.length < 1 || wordIndices.length > 3) {
                throw new ObjReaderException("Invalid element size.", lineInd);
            }

            int vObj = Integer.parseInt(wordIndices[0]);
            onePolygonVertexIndices.add(resolveObjIndex(vObj, verticesCount, lineInd));

            boolean hasVT = false;
            boolean hasVN = false;

            if (wordIndices.length >= 2 && !wordIndices[1].equals("")) {
                int vtObj = Integer.parseInt(wordIndices[1]);
                onePolygonTextureVertexIndices.add(resolveObjIndex(vtObj, textureVerticesCount, lineInd));
                hasVT = true;
            }

            if (wordIndices.length == 3 && !wordIndices[2].equals("")) {
                int vnObj = Integer.parseInt(wordIndices[2]);
                onePolygonNormalIndices.add(resolveObjIndex(vnObj, normalsCount, lineInd));
                hasVN = true;
            }

            if (wordIndices.length == 3 && wordIndices[2].equals("") && !wordIndices[1].equals("")) {
                throw new ObjReaderException("Invalid element size.", lineInd);
            }

            return new FaceFlags(hasVT, hasVN);

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse int value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few arguments.", lineInd);
        }
    }

    protected static int resolveObjIndex(final int objIndex, final int currentSize, final int lineInd) {
        if (objIndex == 0) {
            throw new ObjReaderException("OBJ indices are 1-based; index 0 is invalid.", lineInd);
        }
        if (currentSize <= 0) {
            throw new ObjReaderException("Face index out of bounds.", lineInd);
        }

        final int resolved = (objIndex > 0) ? (objIndex - 1) : (currentSize + objIndex);
        if (resolved < 0 || resolved >= currentSize) {
            throw new ObjReaderException("Face index out of bounds.", lineInd);
        }
        return resolved;
    }


    protected static class DeferredFace {
        final int lineInd;
        final ArrayList<String> words; // без токена "f"
        final int vCountAtTime;
        final int vtCountAtTime;
        final int vnCountAtTime;

        public DeferredFace(int lineInd, ArrayList<String> words, int vCountAtTime, int vtCountAtTime, int vnCountAtTime) {
            this.lineInd = lineInd;
            this.words = words;
            this.vCountAtTime = vCountAtTime;
            this.vtCountAtTime = vtCountAtTime;
            this.vnCountAtTime = vnCountAtTime;
        }

        protected static boolean faceHasNegativeIndex(final ArrayList<String> faceWords) {
            for (String w : faceWords) {
                String[] parts = w.split("/", -1);
                for (String p : parts) {
                    if (p == null || p.isEmpty()) continue;
                    if (p.charAt(0) == '-') return true;
                }
            }
            return false;
        }
    }
}
