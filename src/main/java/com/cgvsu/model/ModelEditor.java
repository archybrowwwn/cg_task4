package com.cgvsu.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelEditor {

    public static void deletePolygon(Model model, int polygonIndex) {
        if (model == null) return;
        if (polygonIndex < 0 || polygonIndex >= model.polygons.size()) return;

        model.polygons.remove(polygonIndex);
        cleanUpUnusedVertices(model);
    }

    public static void deletePolygons(Model model, Set<Integer> polygonIndicesToRemove) {
        if (model == null) return;
        if (polygonIndicesToRemove == null || polygonIndicesToRemove.isEmpty()) return;

        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (int i = 0; i < model.polygons.size(); i++) {
            if (!polygonIndicesToRemove.contains(i)) {
                newPolygons.add(model.polygons.get(i));
            }
        }
        model.polygons = newPolygons;
        cleanUpUnusedVertices(model);
    }

    public static void deleteVertex(Model model, int vertexIndex) {
        if (model == null) return;
        if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) return;

        Set<Integer> polygonsToRemove = new HashSet<>();
        for (int p = 0; p < model.polygons.size(); p++) {
            List<Integer> vIdx = model.polygons.get(p).getVertexIndices();
            for (int i = 0; i < vIdx.size(); i++) {
                if (vIdx.get(i) == vertexIndex) {
                    polygonsToRemove.add(p);
                    break;
                }
            }
        }

        deletePolygons(model, polygonsToRemove);
    }

    public static void cleanUpUnusedVertices(Model model) {
        if (model == null) return;

        int oldCount = model.vertices.size();
        boolean[] used = new boolean[oldCount];

        for (Polygon poly : model.polygons) {
            for (Integer idx : poly.getVertexIndices()) {
                if (idx != null && idx >= 0 && idx < oldCount) {
                    used[idx] = true;
                }
            }
        }

        int[] oldToNew = new int[oldCount];
        ArrayList<com.cgvsu.math.Vector3f> newVertices = new ArrayList<>();

        int newIndex = 0;
        for (int i = 0; i < oldCount; i++) {
            if (used[i]) {
                oldToNew[i] = newIndex;
                newVertices.add(model.vertices.get(i));
                newIndex++;
            } else {
                oldToNew[i] = -1;
            }
        }

        for (Polygon poly : model.polygons) {
            List<Integer> vIdx = poly.getVertexIndices();
            for (int i = 0; i < vIdx.size(); i++) {
                int oldIndex = vIdx.get(i);
                int mapped = oldToNew[oldIndex];
                vIdx.set(i, mapped);
            }
        }

        model.vertices = newVertices;
    }
}
