package com.svamp.planetwars.opengl;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple WaveFront .obj loading class.
 */
public class ObjLoader {

    /**
     * Takes a file in assets and generates a OpenGL-ready array of triangles representing the
     * object (wavefront .obj). Triangles represented in CCW order. This method assumes all faces
     * are triangles, and does not import texture coords ATM.
     *
     * @param c Context for file read rights
     * @param filePointer R.raw id of file
     * @return Array of vertex coordinates representing object
     * @throws IOException
     */
    public static float[] getTriArray(Context c, int filePointer) throws IOException {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(
                c.getResources().openRawResource(filePointer)));
        String line;
        List<String> file = new ArrayList<String>();
        int vertexCount = 0;
        int texCoordCount = 0;
        int faceCount = 0;
        // First, just count vertex number..
        while((line = fileReader.readLine()) != null) {
            if(line.startsWith("v ")) vertexCount++;
            else if(line.startsWith("f ")) faceCount++;
            else if(line.startsWith("vt ")) texCoordCount++;
            else continue; // Ensure we don't add lines to array that we don't need.
            file.add(line);
        }
        fileReader.close();

        // Parse all vertices and face indices
        float[][] vertices = new float[vertexCount][3];
        float[][] texVertices = new float[texCoordCount][2];
        int[][] faceIdxs = new int[faceCount][3];
        int[][] texFaceIdxs = new int[faceCount][3];
        int ptr = 0;
        int ptr2 = 0;
        int ptr3 = 0;

        for(String l : file) {
            if(l.startsWith("v ")) {
                String[] vertex = l.split(" ");
                vertices[ptr][0] = Float.parseFloat(vertex[1]);
                vertices[ptr][1] = Float.parseFloat(vertex[2]);
                vertices[ptr][2] = Float.parseFloat(vertex[3]);
                ptr++;
            } else if(l.startsWith("vt ")) {
                String[] texVertex = l.split(" ");
                texVertices[ptr2][0] = Float.parseFloat(texVertex[1]);
                texVertices[ptr2][1] = Float.parseFloat(texVertex[2]);
                ptr2++;
            } else if(l.startsWith("f ")) {
                String[] face = l.split(" ");
                // Face indices are 1-indexed.
                for(int i=0;i<3;i++) {
                    String[] faceTexPair = face[i+1].split("/");
                    faceIdxs[ptr3][i] =    Integer.parseInt(faceTexPair[0]) - 1;
                    texFaceIdxs[ptr3][i] = Integer.parseInt(faceTexPair[1]) - 1;
                }
                ptr3++;
            }
        }
        //Build tri array:
        float[] triArray = new float[vertexCount*3*faceCount];
        float[] triTexArray = new float[texCoordCount*2*faceCount];
        for(int faceNum = 0 ; faceNum<faceCount ; faceNum++) {
            for(int vertNum = 0 ; vertNum<3; vertNum++) {
                for(int i = 0; i < 3 ; i++) {
                    triArray[faceNum*9 + vertNum*3 + i] = vertices[faceIdxs[faceNum][vertNum]][i];
                }
                triTexArray[faceNum*6 + vertNum*3 + 0] = texVertices[texFaceIdxs[faceNum][vertNum]][0];
                triTexArray[faceNum*6 + vertNum*3 + 1] = texVertices[texFaceIdxs[faceNum][vertNum]][1];
            }
        }
        return triArray;
    }
}
