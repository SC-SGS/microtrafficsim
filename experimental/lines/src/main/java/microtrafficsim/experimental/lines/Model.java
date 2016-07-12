package microtrafficsim.experimental.lines;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;

import java.nio.FloatBuffer;


class Model {
    private float   angle1;
    private float   angle2;
    private boolean dirty;

    private VertexArrayObject vao;
    private BufferStorage     vbo;

    Model() {
        this.angle1 = 0;
        this.angle2 = 180;
        this.dirty  = false;

        this.vao = null;
        this.vbo = null;
    }


    void setAngle1(float angle) {
        this.angle1 = angle;
        this.dirty  = true;
    }

    void setAngle2(float angle) {
        this.angle2 = angle;
        this.dirty  = true;
    }


    void initialize(GL2GL3 gl) {
        // initialize vertex buffer
        FloatBuffer vertices = getVertexBuffer(angle1, angle2);

        // create and load vbo
        vbo = BufferStorage.create(gl, GL2GL3.GL_ARRAY_BUFFER);
        vbo.bind(gl);
        gl.glBufferData(GL2GL3.GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GL2GL3.GL_STATIC_DRAW);
        vbo.unbind(gl);

        // create and initialize vao
        VertexAttributePointer ptrPosition
                = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, vbo, 0, 0);
        assert ptrPosition != null;

        vao = VertexArrayObject.create(gl);
        vao.bind(gl);
        vbo.bind(gl);
        ptrPosition.set(gl);
        ptrPosition.enable(gl);
        vbo.unbind(gl);
    }

    void dispose(GL2GL3 gl) {
        vao.dispose(gl);
        vbo.dispose(gl);
    }

    void display(GL2GL3 gl) {
        if (dirty) update(gl);

        vao.bind(gl);
        gl.glDrawArrays(GL3.GL_LINE_STRIP_ADJACENCY, 0, 5);
        vao.unbind(gl);
    }

    private void update(GL2GL3 gl) {
        FloatBuffer vertices = getVertexBuffer(angle1, angle2);

        vbo.bind(gl);
        gl.glBufferData(GL2GL3.GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GL2GL3.GL_STATIC_DRAW);
        vbo.unbind(gl);

        dirty = false;
    }

    private FloatBuffer getVertexBuffer(float angle1, float angle2) {
        float x1 = (float) Math.cos(Math.toRadians(angle1));
        float y1 = (float) Math.sin(Math.toRadians(angle1));
        float x2 = (float) Math.cos(Math.toRadians(angle2));
        float y2 = (float) Math.sin(Math.toRadians(angle2));

        FloatBuffer vertices = FloatBuffer.allocate(15);
        vertices.put(x1);
        vertices.put(y1);
        vertices.put(0.0f);
        vertices.put(x1);
        vertices.put(y1);
        vertices.put(0.0f);
        vertices.put(0.0f);
        vertices.put(0.0f);
        vertices.put(0.0f);
        vertices.put(x2);
        vertices.put(y2);
        vertices.put(0.0f);
        vertices.put(x2);
        vertices.put(y2);
        vertices.put(0.0f);
        vertices.rewind();

        return vertices;
    }
}
