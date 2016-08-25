package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.glsl.ShaderUtil;
import microtrafficsim.core.vis.exceptions.ResourceError;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;
import microtrafficsim.utils.Streams;
import microtrafficsim.utils.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Wrapper for an OpenGL shader.
 *
 * @author Maximilian Luz
 */
public class Shader {

    protected int    type;
    protected int    handle;
    protected String name;

    protected String[] source;

    protected HashSet<ShaderProgram>            programs;
    protected HashSet<LifeTimeObserver<Shader>> ltObservers;


    /**
     * Creates a new OpenGL shader for the givne type and name and wraps it in a {@code Shader}.
     *
     * @param gl   the {@code GL2ES2}-Object of the OpenGL context.
     * @param type the (OpenGL) type-id of the shader.
     * @param name the (unique) name of the shader that can be used to identify it.
     * @return the created OpenGL shader wrapped as {@code Shader}.
     */
    public static Shader create(GL2ES2 gl, int type, String name) {
        int handle = gl.glCreateShader(type);
        return new Shader(type, handle, name);
    }

    /**
     * Constructs a new {@code Shader} wrapper for the given shader program.
     *
     * @param type   the OpenGL type-id of the shader.
     * @param handle the OpenGL handle of the shader.
     * @param name   the (unique) name of the shader that can be used to identify it.
     */
    protected Shader(int type, int handle, String name) {
        this.type        = type;
        this.handle      = handle;
        this.name        = name;
        this.source      = null;
        this.programs    = new HashSet<>();
        this.ltObservers = new HashSet<>();
    }

    /**
     * Disposes the wrapped OpenGL shader object.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    public void dispose(GL2ES2 gl) {
        gl.glDeleteShader(handle);
        handle = -1;

        for (LifeTimeObserver<Shader> lto : ltObservers)
            lto.disposed(this);
    }


    /**
     * Returns the name of the shader.
     *
     * @return the name of the shader.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the shader.
     *
     * @return the OpenGL type-id of the shader.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the handle of the wrapped shader.
     *
     * @return the OpenGL handle of the wrapped shader.
     */
    public int getHandle() {
        return handle;
    }


    /**
     * Sets the source-code of this shader.
     *
     * @param source the (new) source-code of this shader.
     * @return this {@code Shader}.
     */
    public Shader setSource(String source) {
        this.source = new String[1];
        return this;
    }

    /**
     * Sets the source-code of this shader.
     *
     * @param source the (new) source-code of this shader as array of strings including new-line characters.
     * @return this {@code Shader}.
     */
    public Shader setSource(String[] source) {
        this.source = source;
        return this;
    }

    /**
     * Loads the shader source code from the given resource.
     *
     * @param resource the resource to load the source code from.
     * @return this {@code Shader}.
     */
    public Shader loadFromResource(Resource resource) {
        try (InputStream in = resource.asStream()) {
            source = Streams.toStringArrayEOL(in);
        } catch (IOException | NullPointerException e) { throw new ResourceError(resource.toString(), e); }

        return this;
    }

    /**
     * Returns the source code of this shader.
     *
     * @return the source code of this shader.
     */
    public String[] getSource() {
        return source;
    }

    /**
     * Compiles this shader program.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     * @return this {@code Shader}.
     * @throws ShaderCompileException if the compilation of this shader failed.
     */
    public Shader compile(GL2ES2 gl) throws ShaderCompileException {
        gl.glShaderSource(handle, source.length, source, null);
        gl.glCompileShader(handle);

        // check status
        int[] status = {0};
        gl.glGetShaderiv(handle, GL2ES2.GL_COMPILE_STATUS, status, 0);
        if (status[0] == GL2ES2.GL_FALSE)
            throw new ShaderCompileException(name, source, ShaderUtil.getShaderInfoLog(gl, handle));

        return this;
    }


    /**
     * Add the given {@code ShaderProgram} to the set of programs making use of this shader. This set is maintained to
     * forward change/update-notifications on changes to this shader.
     *
     * @param program the shader program to add.
     */
    public void addReferencedProgram(ShaderProgram program) {
        programs.add(program);
    }

    /**
     * Removes the given {@code ShaderProgram} from the set of programs making use of this shader. This set is
     * maintained to forward change/update-notifications on changes to this shader.
     *
     * @param gl      the {@code GL2ES2}-Object of the OpenGL context.
     * @param program the shader program to remove.
     */
    public void removeReferencedProgram(GL2ES2 gl, ShaderProgram program) {
        programs.remove(program);
    }

    /**
     * Returns the set of {@code ShaderProgram}s making use of this shader. This set is maintained to forward
     * change/update-notifications on changes to this shader.
     *
     * @return the set of programs making use of this shader.
     */
    public Set<ShaderProgram> getReferencedPrograms() {
        return Collections.unmodifiableSet(programs);
    }


    /**
     * Adds an observer that is being notified when the life-time of the wrapped OpenGL shader ends, i.e.
     * a {@link Shader#dispose(GL2ES2)} call has been made.
     *
     * @param lto the {@code LifeTimeObserver} to add.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    public boolean addLifeTimeObserver(LifeTimeObserver<Shader> lto) {
        return ltObservers.add(lto);
    }

    /**
     * Removes the specified {@code LifeTimeObserver} from the set of observers for this shader.
     *
     * @param lto the {@code LifeTimeObserver} to remove.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    public boolean removeLifeTimeObserver(LifeTimeObserver<Shader> lto) {
        return ltObservers.remove(lto);
    }

    /**
     * Returns all {@code LifeTimeObservers} observing the life time of this shader.
     *
     * @return all {@code LifeTimeObservers} observing the life time of this shader.
     */
    public Set<LifeTimeObserver<Shader>> getLifeTimeObservers() {
        return Collections.unmodifiableSet(ltObservers);
    }
}
