package com.svamp.planetwars.opengl;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class GLModelBuilder {
    protected static final int BYTES_PER_FLOAT = 4;

    private GL10 glUnused;
    private float[] verts;
    private float[] texCoords;
    private int[] indices;

    public GLModelBuilder(GL10 glUnused) {
        this.glUnused = glUnused;
    }

    public GLModelBuilder setVertices(float[] verts) {
        this.verts = verts;
        return this;
    }
    public GLModelBuilder setTextureCoords(float[] texCoords) {
        this.texCoords = texCoords;
        return this;
    }
    public GLModelBuilder setIndices(int[] indices) {
        this.indices = indices;
        return this;
    }

    public VBOGlModel buildVboModel(GL10 glUnused) {
        GlModel model = buildModel();
        VBOGlModel vboGlModel = new VBOGlModel(model.drawBuffer, model.indices);
        vboGlModel.bufferInGl(glUnused);
        return vboGlModel;
    }

    public GlModel buildModel() {
        if(verts == null || texCoords == null) {
            throw new IllegalStateException("Tried to build a GlModel without vertices or " +
                    "texture coords!");
        }

        FloatBuffer drawBuffer = ByteBuffer
                .allocateDirect((verts.length + texCoords.length) * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        // Merge the coordinate data..:
        for(int i=0; i<texCoords.length/2; i++) {
            drawBuffer
                    .put(verts[i*3])
                    .put(verts[i*3+1])
                    .put(verts[i*3+2])
                    .put(texCoords[i*2])
                    .put(texCoords[i*2+1]);
        }
        // Depending on the number of vertices, determine how we may represent the indexes
        // best.
        if(indices == null) { return new GlModel(drawBuffer, null); }

        if(verts.length < 1 << 7) {
            ByteBuffer buf = ByteBuffer
                    .allocateDirect(indices.length)
                    .order(ByteOrder.nativeOrder());
            for(int index : indices)
                buf.put((byte)index);
            return new GlModel(drawBuffer, buf);
        } else if(verts.length < 1 << 15) {
            ShortBuffer buf = ByteBuffer
                    .allocateDirect(indices.length*2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            for(int index : indices)
                buf.put((short)index);
            return new GlModel(drawBuffer, buf);
        } else {
            IntBuffer buf = ByteBuffer
                    .allocateDirect(indices.length*4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
            for(int index : indices)
                buf.put(index);
            return new GlModel(drawBuffer, buf);
        }
    }
}
