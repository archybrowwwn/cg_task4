package com.cgvsu.objwriter;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;

public class ObjWriter {

    private ObjWriter() {}

    private static final String OBJ_VERTEX_TOKEN  = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN  = "vn";
    private static final String OBJ_FACE_TOKEN    = "f";

    public static String write(final Model model) {
        if (model == null) {
            throw new ObjWriterException("Model is null.");
        }
        if (model.vertices == null || model.vertices.isEmpty()) {
            throw new ObjWriterException("Model has no vertices.");
        }
        if (model.polygons == null) {
            throw new ObjWriterException("Polygons list is null.");
        }

        final StringBuilder sb = new StringBuilder(1024);

        for (int i = 0; i < model.vertices.size(); i++) {
            final Vector3f v = model.vertices.get(i);
            if (v == null) {
                throw new ObjWriterException("Vertex is null at index: " + i);
            }
            sb.append(OBJ_VERTEX_TOKEN).append(' ')
                    .append(v.x).append(' ').append(v.y).append(' ').append(v.z)
                    .append('\n');
        }

        if (model.textureVertices != null && !model.textureVertices.isEmpty()) {
            for (int i = 0; i < model.textureVertices.size(); i++) {
                final Vector2f vt = model.textureVertices.get(i);
                if (vt == null) {
                    throw new ObjWriterException("Texture vertex is null at index: " + i);
                }
                sb.append(OBJ_TEXTURE_TOKEN).append(' ')
                        .append(vt.x).append(' ').append(vt.y)
                        .append('\n');
            }
        }


        if (model.normals != null && !model.normals.isEmpty()) {
            for (int i = 0; i < model.normals.size(); i++) {
                final Vector3f vn = model.normals.get(i);
                if (vn == null) {
                    throw new ObjWriterException("Normal is null at index: " + i);
                }
                sb.append(OBJ_NORMAL_TOKEN).append(' ')
                        .append(vn.x).append(' ').append(vn.y).append(' ').append(vn.z)
                        .append('\n');
            }
        }

        for (int p = 0; p < model.polygons.size(); p++) {
            final Polygon poly = model.polygons.get(p);
            if (poly == null) {
                throw new ObjWriterException("Polygon is null.", p);
            }

            final ArrayList<Integer> vIdx = poly.getVertexIndices();
            if (vIdx == null || vIdx.size() < 3) {
                throw new ObjWriterException("Polygon has < 3 vertex indices.", p);
            }

            final ArrayList<Integer> vtIdx = poly.getTextureVertexIndices();
            final ArrayList<Integer> vnIdx = poly.getNormalIndices();

            final boolean hasVT = (vtIdx != null && !vtIdx.isEmpty());
            final boolean hasVN = (vnIdx != null && !vnIdx.isEmpty());

            if (hasVT && vtIdx.size() != vIdx.size()) {
                throw new ObjWriterException("Inconsistent texture indices count (v=" + vIdx.size() + ", vt=" + vtIdx.size() + ").", p);
            }
            if (hasVN && vnIdx.size() != vIdx.size()) {
                throw new ObjWriterException("Inconsistent normal indices count (v=" + vIdx.size() + ", vn=" + vnIdx.size() + ").", p);
            }

            sb.append(OBJ_FACE_TOKEN);

            for (int k = 0; k < vIdx.size(); k++) {
                final int vi0 = vIdx.get(k);
                if (vi0 < 0 || vi0 >= model.vertices.size()) {
                    throw new ObjWriterException("Vertex index out of bounds: " + vi0 + " (size=" + model.vertices.size() + ")", p);
                }

                final int vi = vi0 + 1;

                if (!hasVT && !hasVN) {
                    sb.append(' ').append(vi);
                    continue;
                }

                if (hasVT && !hasVN) {
                    final int vti0 = vtIdx.get(k);
                    if (vti0 < 0 || vti0 >= model.textureVertices.size()) {
                        throw new ObjWriterException("Texture index out of bounds: " + vti0 + " (size=" + model.textureVertices.size() + ")", p);
                    }
                    sb.append(' ').append(vi).append('/').append(vti0 + 1);
                    continue;
                }

                if (!hasVT && hasVN) {
                    final int vni0 = vnIdx.get(k);
                    if (vni0 < 0 || vni0 >= model.normals.size()) {
                        throw new ObjWriterException("Normal index out of bounds: " + vni0 + " (size=" + model.normals.size() + ")", p);
                    }
                    sb.append(' ').append(vi).append('/').append('/').append(vni0 + 1);
                    continue;
                }

                final int vti0 = vtIdx.get(k);
                final int vni0 = vnIdx.get(k);

                if (vti0 < 0 || vti0 >= model.textureVertices.size()) {
                    throw new ObjWriterException("Texture index out of bounds: " + vti0 + " (size=" + model.textureVertices.size() + ")", p);
                }
                if (vni0 < 0 || vni0 >= model.normals.size()) {
                    throw new ObjWriterException("Normal index out of bounds: " + vni0 + " (size=" + model.normals.size() + ")", p);
                }

                sb.append(' ')
                        .append(vi).append('/')
                        .append(vti0 + 1).append('/')
                        .append(vni0 + 1);
            }

            sb.append('\n');
        }

        return sb.toString();
    }
}