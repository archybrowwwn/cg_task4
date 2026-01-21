package com.cgvsu.objwriter;

public class ObjWriterException extends RuntimeException {
    public ObjWriterException(String errorMessage) {
        super("Error writing OBJ file. " + errorMessage);
    }

    public ObjWriterException(String errorMessage, int polygonInd) {
        super("Error writing OBJ file on polygon: " + polygonInd + ". " + errorMessage);
    }
}
