package microtrafficsim.core.vis.context;

import java.util.HashMap;

import com.jogamp.opengl.GL2ES2;

import microtrafficsim.core.vis.opengl.shader.ManagedShader;
import microtrafficsim.core.vis.opengl.shader.ManagedShaderProgram;
import microtrafficsim.core.vis.opengl.shader.Shader;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;


public class ShaderManager {

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


    public ShaderManager() {
        this.shaders  = new HashMap<>();
        this.programs = new HashMap<>();
    }


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

    public ManagedShader getShader(String key) {
        ManagedShader s = shaders.get(key);

        if (s != null && s.getHandle() == -1)
            return null;
        else
            return s;
    }

    public ManagedShader removeShader(String key) {
        ManagedShader s = shaders.remove(key);

        if (s != null) s.removeLifeTimeObserver(shaderLto);

        if (s != null && s.getHandle() == -1)
            return null;
        else
            return s;
    }

    public boolean hasShader(String key) {
        ManagedShader s = shaders.get(key);
        return s != null && s.getHandle() != -1;
    }


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

    public ManagedShaderProgram getProgram(String key) {
        ManagedShaderProgram p = programs.get(key);

        if (p != null && p.getHandle() == -1)
            return null;
        else
            return p;
    }

    public ManagedShaderProgram removeProgram(String key) {
        ManagedShaderProgram p = programs.remove(key);

        if (p != null)
            p.removeLifeTimeObserver(programLto);

        if (p != null && p.getHandle() == -1)
            return null;
        else
            return p;
    }

    public boolean hasProgram(String key) {
        ManagedShaderProgram p = programs.get(key);
        return p != null && p.getHandle() != -1;
    }


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
