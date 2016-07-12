package microtrafficsim.experimental.lines;

import com.jogamp.opengl.GL2GL3;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Vec3f;


class ModelInstance {

    private Model base;

    private Vec3f position;
    private float scale;

    private Color   color;
    private int     polygonMode;
    private float   linewidth;
    private int     cap;
    private int     join;
    private boolean blend;

    private Mat4f model;


    ModelInstance(Model base, Vec3f position, float scale, Color color, int polygonMode, float linewidth, int cap,
                  int join, boolean blend) {
        this.base = base;

        this.position = position;
        this.scale    = scale;

        this.color       = color;
        this.polygonMode = polygonMode;
        this.linewidth   = linewidth;
        this.cap         = cap;
        this.join        = join;
        this.blend       = blend;

        this.model = Mat4f.identity();

        updateMatrix();
    }

    private void updateMatrix() {
        this.model.set(Mat4f.identity().translate(position).scale(scale, scale, scale));
    }

    void display(RenderContext context, Uniforms uniforms) {
        GL2GL3 gl = context.getDrawable().getGL().getGL2GL3();

        uniforms.uModelMatrix.set(model);
        uniforms.uColor.set(color);
        uniforms.uLineWidth.set(linewidth);
        uniforms.uCapType.set(cap);
        uniforms.uJoinType.set(join);

        if (blend)
            context.BlendMode.enable(gl);
        else
            context.BlendMode.disable(gl);

        gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, polygonMode);

        base.display(gl);
    }
}
