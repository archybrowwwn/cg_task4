package com.cgvsu.model;

import com.cgvsu.objreader.ObjReaderException;

import java.util.ArrayList;

public class Polygon {

    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureVertexIndices;
    private ArrayList<Integer> normalIndices;


    public Polygon() {
        vertexIndices = new ArrayList<Integer>();
        textureVertexIndices = new ArrayList<Integer>();
        normalIndices = new ArrayList<Integer>();
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        if (vertexIndices == null || vertexIndices.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertex indices");
        }
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        if (textureVertexIndices == null) {
            this.textureVertexIndices = new ArrayList<>();
            return;
        }
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        if (normalIndices == null) {
            this.normalIndices = new ArrayList<>();
            return;
        }
        this.normalIndices = normalIndices;
    }

    protected static int resolveObjIndex(final int objIndex, final int currentSize, final int lineInd) {
        if (objIndex == 0) {
            throw new ObjReaderException("OBJ indices are 1-based; index 0 is invalid.", lineInd);
        }

        int resolved;
        if (objIndex > 0) {
            resolved = objIndex - 1;
        } else {
            resolved = currentSize + objIndex; // objIndex отрицательный
        }

        if (resolved < 0 || resolved >= currentSize) {
            throw new ObjReaderException("Face index out of bounds.", lineInd);
        }
        return resolved;
    }

    public ArrayList<Integer> getVertexIndices()                                                                                                                                                                                                                                     {
        return vertexIndices;
    }

    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }
}
