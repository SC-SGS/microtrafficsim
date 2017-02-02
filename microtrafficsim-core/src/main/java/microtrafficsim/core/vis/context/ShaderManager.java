package microtrafficsim.core.vis.context;

import java.io.IOException;
import java.util.HashMap;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.shader.*;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;


/**
 * Manager for {@code Shader}s and {@code ShaderProgram}s.
 *
 * @author Maximilian Luz
 */
public class ShaderManager {

    private RenderContext context;
    private HashMap<String, ManagedShader>        shaders;
    private HashMap<String, ManagedShaderProgram> programs;

    private LifeTimeObserver<Shader> shaderLto = new LifeTimeObserver<Shader>() {
        @Override
        public void disposed(Shader obj) {
            shaders.remove(obj);
        }
    };

    private LifeTimeObserver<ShaderProgram> programLto = new LifeTimeObserver<ShaderProgram>() {
        @Override
        public void disposed(ShaderProgram obj) {
            programs.remove(obj);
        }
    };


    /**
     * Constructs a new (empty) {@code ShaderManager}.
     */
    public ShaderManager(RenderContext context) {
        this.context = context;
        this.shaders  = new HashMap<>();
        this.programs = new HashMap<>();
    }


    public ManagedShaderProgram load(ShaderProgramSource source) throws IOException, ShaderCompileException, ShaderLinkException {
        ManagedShaderProgram program = requireProgram(source.getName());
        if (program != null) return program;

        program = ManagedShaderProgram.create(context, source.getName());

        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();

        for (ShaderSource src : source.getSources()) {
            Shader shader = load(src);
            program.attach(gl, shader);
        }

        program.link(gl);

        /* NOTE:
         * 	Shaders are not detached to keep them in memory and avoid re-compilation.
         */

        putProgram(source.getName(), program);
        return program;
    }

    public ManagedShader load(ShaderSource source) throws IOException, ShaderCompileException {
        ManagedShader shader = requireShader(source.resource.getUniqueName());
        if (shader != null) return shader;

        GL2ES2 gl = context.getDrawable().getGL().getGL2ES2();
        shader = ManagedShader.create(gl, source.type, source.resource.getUniqueName())
                .loadFromResource(source.resource)
                .compile(gl);

        putShader(source.resource.getUniqueName(), shader);
        return shader;
    }


    /**
     * Associates the given shader with the given key.
     *
     * @param key    the key to associate the shader with.
     * @param shader the shader to add to this manager.
     * @return the shader previously associated with he given key or {@code null} if no such shader exists.
     */
    public ManagedShader putShader(String key, ManagedShader shader) {
        ManagedShader old = shaders.put(key, shader);

        shader.addLifeTimeObserver(shaderLto);

        if (old != null)
            shader.removeLifeTimeObserver(shaderLto);

        if (old != null && old.getHandle() == -1)
            return null;
        else
            return old;
    }

    /**
     * Returns the shader associated with the given key and increments its reference-counter.
     *
     * @param key the key to return the shader for.
     * @return the shader associated with the given key or {@code null} if no such shader exists.
     */
    public ManagedShader requireShader(String key) {
        ManagedShader s = shaders.get(key);

        if (s != null)
            return s.getHandle() != -1 ? s.require() : null;
        else
            return null;
    }

    /**
     * Removes the shader associated with the given key.
     *
     * @param key the key to remove the shader for.
     * @return the shader previously associated with the given key or {@code null} if no such shader exists.
     */
    public ManagedShader removeShader(String key) {
        ManagedShader s = shaders.remove(key);

        if (s != null) s.removeLifeTimeObserver(shaderLto);

        if (s != null && s.getHandle() == -1)
            return null;
        else
            return s;
    }

    /**
     * Checks if there exits a shader associated with the given key.
     *
     * @param key the key for which the check should be done.
     * @return {@code true} if there is a shader associated with the given key or {@code null} if not.
     */
    public boolean hasShader(String key) {
        ManagedShader s = shaders.get(key);
        return s != null && s.getHandle() != -1;
    }


    /**
     * Associates the given shader-program with the given key.
     *
     * @param key     the key to associate the shader-program with.
     * @param program the shader-program to add to this manager.
     * @return the shader-program previously associated with he given key or {@code null} if no such program exists.
     */
    public ManagedShaderProgram putProgram(String key, ManagedShaderProgram program) {
        ManagedShaderProgram old = programs.put(key, program);

        program.addLifeTimeObserver(programLto);

        if (old != null)
            old.removeLifeTimeObserver(programLto);

        if (old != null && old.getHandle() == -1)
            return null;
        else
            return old;
    }

    /**
     * Returns the shader-program associated with the given key and increments its reference-counter.
     *
     * @param key the key to return the program for.
     * @return the program associated with the given key or {@code null} if no such program exists.
     */
    public ManagedShaderProgram requireProgram(String key) {
        ManagedShaderProgram p = programs.get(key);

        if (p != null)
            return p.getHandle() != -1 ? p.require() : null;
        else
            return null;
    }

    /**
     * Removes the shader-program associated with the given key.
     *
     * @param key the key to remove the shader-program for.
     * @return the program previously associated with the given key or {@code null} if no such program exists.
     */
    public ManagedShaderProgram removeProgram(String key) {
        ManagedShaderProgram p = programs.remove(key);

        if (p != null)
            p.removeLifeTimeObserver(programLto);

        if (p != null && p.getHandle() == -1)
            return null;
        else
            return p;
    }

    /**
     * Checks if there exits a shader-program associated with the given key.
     *
     * @param key the key for which the check should be done.
     * @return {@code true} if there is a shader-program associated with the given key or {@code null} if not.
     */
    public boolean hasProgram(String key) {
        ManagedShaderProgram p = programs.get(key);
        return p != null && p.getHandle() != -1;
    }


    /**
     * Disposes this manager.
     *
     * @param gl    the {@code GL2ES2}-Object of the OpenGL context.
     * @param force set to {@code true} if the shaders and shader-programs should be disposed forcefully
     * @see ManagedShader#dispose(GL2ES2, boolean)
     * @see ManagedShaderProgram#dispose(GL2ES2, boolean)
     */
    public void dispose(GL2ES2 gl, boolean force) {
        for (ManagedShader s : shaders.values()) {
            s.removeLifeTimeObserver(shaderLto);
            s.dispose(gl, force);
        }

        for (ManagedShaderProgram p : programs.values()) {
            p.removeLifeTimeObserver(programLto);
            p.dispose(gl, force);
        }
    }
}
