package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

import java.util.ArrayList;

public class ModelPreprocessor {

    public static void triangulate(final Model model) {
        ArrayList<Polygon> triangulated = new ArrayList<>();

        for (Polygon poly : model.polygons) {
            ArrayList<Integer> v = poly.getVertexIndices();
            ArrayList<Integer> t = poly.getTextureVertexIndices();
            ArrayList<Integer> n = poly.getNormalIndices();

            int size = v.size();
            if (size < 3) continue;

            boolean hasTex = (t != null && !t.isEmpty());
            boolean hasNorm = (n != null && !n.isEmpty());

            int v0 = v.get(0);
            Integer t0 = hasTex ? t.get(0) : null;
            Integer n0 = hasNorm ? n.get(0) : null;

            for (int i = 1; i < size - 1; i++) {
                Polygon tri = new Polygon();

                ArrayList<Integer> triV = new ArrayList<>(3);
                triV.add(v0);
                triV.add(v.get(i));
                triV.add(v.get(i + 1));
                tri.setVertexIndices(triV);

                if (hasTex) {
                    ArrayList<Integer> triT = new ArrayList<>(3);
                    triT.add(t0);
                    triT.add(t.get(i));
                    triT.add(t.get(i + 1));
                    tri.setTextureVertexIndices(triT);
                }

                if (hasNorm) {
                    ArrayList<Integer> triN = new ArrayList<>(3);
                    triN.add(n0);
                    triN.add(n.get(i));
                    triN.add(n.get(i + 1));
                    tri.setNormalIndices(triN);
                }

                triangulated.add(tri);
            }
        }

        model.polygons = triangulated;
    }

    public static void recalculateNormals(final Model model) {
        int vCount = model.vertices.size();

        ArrayList<Vector3f> accum = new ArrayList<>(vCount);
        for (int i = 0; i < vCount; i++) {
            accum.add(new Vector3f(0, 0, 0));
        }

        for (Polygon tri : model.polygons) {
            ArrayList<Integer> idx = tri.getVertexIndices();
            if (idx.size() != 3) continue;

            int ia = idx.get(0);
            int ib = idx.get(1);
            int ic = idx.get(2);

            Vector3f a = model.vertices.get(ia);
            Vector3f b = model.vertices.get(ib);
            Vector3f c = model.vertices.get(ic);

            Vector3f ab = Vector3f.subtract(b, a);
            Vector3f ac = Vector3f.subtract(c, a);
            Vector3f faceN = Vector3f.cross(ac, ab);
            faceN = new Vector3f(-faceN.x, -faceN.y, -faceN.z);

            accum.set(ia, accum.get(ia).add(faceN));
            accum.set(ib, accum.get(ib).add(faceN));
            accum.set(ic, accum.get(ic).add(faceN));
        }

        model.normals.clear();
        for (int i = 0; i < vCount; i++) {
            Vector3f nn = accum.get(i);
            nn.normalize();
            model.normals.add(nn);
        }

        for (Polygon tri : model.polygons) {
            ArrayList<Integer> vIdx = tri.getVertexIndices();
            tri.setNormalIndices(new ArrayList<>(vIdx));
        }
    }
}