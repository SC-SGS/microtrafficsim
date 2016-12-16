package microtrafficsim.core.vis.opengl.shader;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.glsl.ShaderUtil;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.DataType;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttribute;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.*;

import static microtrafficsim.build.BuildSetup.DEBUG_CORE_VIS;


/**
 * Wrapper for OpenGL shader programs.
 *
 * @author Maximilian Luz
 */
public class ShaderProgram {
    protected static final Logger logger = new EasyMarkableLogger(ShaderProgram.class);

    protected final RenderContext context;

    protected final String name;
    protected int          handle;
    protected GL2ES2       bound;
    protected boolean      dirty;

    protected HashMap<String, VertexAttribute> attribBindings;
    protected ArrayList<Shader>                shaders;
    protected HashMap<String, UniformBinding>  uniforms;
    protected HashMap<String, VertexAttribute> attributes;

    protected HashSet<LifeTimeObserver<ShaderProgram>> ltObservers;


    /**
     * Creates a new OpenGL shader program and wraps it as {@code ShaderProgram}.
     *
     * @param context the context on which the program should be created.
     * @param name    the (unique) name of the program, used to identify it.
     * @return the created {@code ShaderProgram}.
     */
    public static ShaderProgram create(RenderContext context, String name) {
        return new ShaderProgram(context, context.getDrawable().getGL().getGL2ES2().glCreateProgram(), name);
    }

    /**
     * Constructs a new wrapper for the given shader program.
     *
     * @param context the context on which the shader program has been created.
     * @param handle  the handle of the existing OpenGL shader program.
     * @param name    the (unique) name of the program, used to identify it.
     */
    protected ShaderProgram(RenderContext context, int handle, String name) {
        this.context = context;
        this.handle  = handle;
        this.name    = name;

        this.bound = null;
        this.dirty = true;

        this.attribBindings = new HashMap<>();

        this.shaders    = new ArrayList<>();
        this.uniforms   = new HashMap<>();
        this.attributes = new HashMap<>();

        this.ltObservers = new HashSet<>();
    }

    /**
     * Disposes the wrapped OpenGL shader program.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
     */
    public void dispose(GL2ES2 gl) {
        for (UniformBinding binding : uniforms.values())
            binding.var.removeOwner(this);

        for (Shader s : shaders) {
            gl.glDetachShader(handle, s.getHandle());
            s.removeReferencedProgram(gl, this);
        }

        attribBindings.clear();

        shaders.clear();
        uniforms.clear();
        attributes.clear();

        if (handle != -1) {
            gl.glDeleteProgram(handle);
            handle = -1;
        }

        for (LifeTimeObserver<ShaderProgram> lto : ltObservers)
            lto.disposed(this);
    }


    /**
     * Returns the name of this program.
     *
     * @return the name of this {@code ShaderProgram}.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the OpenGL handle of the wrapped program.
     *
     * @return the OpenGL handle of the wrapped program.
     */
    public int getHandle() {
        return handle;
    }


    /**
     * Attaches the specified shaders to this shader program.
     *
     * @param gl      the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
     * @param shaders the (compiled) shaders to attach.
     * @return this {@code ShaderProgram}.
     */
    public ShaderProgram attach(GL2ES2 gl, Shader... shaders) {
        for (Shader s : shaders) {
            gl.glAttachShader(handle, s.getHandle());
            this.shaders.add(s);
            s.addReferencedProgram(this);
        }

        dirty = true;
        return this;
    }

    /**
     * Detaches the specified shaders from this shader program.
     *
     * @param gl      the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
     * @param shaders the shaders to detach.
     * @return this {@code ShaderProgram}.
     */
    public ShaderProgram detach(GL2ES2 gl, Shader... shaders) {
        for (Shader s : shaders) {
            gl.glDetachShader(handle, s.getHandle());
            this.shaders.remove(s);
            s.removeReferencedProgram(gl, this);
        }

        return this;
    }

    /**
     * Return all attached shaders of this program.
     *
     * @return the shaders currently attached to this program.
     */
    public List<Shader> getAttachedShaders() {
        return Collections.unmodifiableList(shaders);
    }


    /**
     * Links the shader program and loads all active uniform- and attribute-bindings.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context on which the program has been created.
     * @throws ShaderLinkException if the shader program cannot be linked.
     * @return this {@code ShaderProgram}.
     */
    public ShaderProgram link(GL2ES2 gl) throws ShaderLinkException {
        // bind attributes
        HashMap<String, VertexAttribute> attribBindings = new HashMap<>(this.attribBindings);
        attribBindings.putAll(context.getVertexAttribManager().getDefaultAttributeBindings());
        for (Map.Entry<String, VertexAttribute> e : attribBindings.entrySet())
            gl.glBindAttribLocation(handle, e.getValue().index, e.getKey());

        // link program
        gl.glLinkProgram(handle);

        // check link status
        int[] status = {0};
        gl.glGetProgramiv(handle, GL2ES2.GL_LINK_STATUS, status, 0);
        if (status[0] == GL2ES2.GL_FALSE) throw new ShaderLinkException(name, ShaderUtil.getProgramInfoLog(gl, handle));

        // reload active attributes and uniforms
        reloadActiveAttributes(gl);
        reloadActiveUniforms(gl);

        dirty = false;
        return this;
    }


    /**
     * Adds/sets the attribute binding for the given attribute-name to the given {@code VertexAttribute}.
     *
     * @param name      the name of the attribute, as used in the GLSL shader code.
     * @param attribute the {@code VertexAttribute} to bind the name to.
     * @return the {@code VertexAttribute} previously bound to the given name or {@code null} if none was bound.
     */
    public VertexAttribute putAttributeBinding(String name, VertexAttribute attribute) {
        if (attributes.containsKey(name)) dirty = true;
        return attribBindings.put(name, attribute);
    }

    /**
     * Removes the attribute binding for the given attribute-name.
     *
     * @param name the name of the attribute, as used in the GLSL shader code.
     * @return the {@code VertexAttribute} previously bound to the given name or {@code null} if none was bound.
     */
    public VertexAttribute removeAttributeBinding(String name) {
        if (attributes.containsKey(name)) dirty = true;
        return attribBindings.remove(name);
    }

    /**
     * Returns the attribute binding for the given attribute-name.
     *
     * @param name the name of the attribute, as used in the GLSL shader code.
     * @return the {@code VertexAttribute} bound to the given name or {@code null} if none is bound.
     */
    public VertexAttribute getAttributeBinding(String name) {
        return attribBindings.get(name);
    }

    /**
     * Returns all (manually set) attribute bindings for this shader program.
     *
     * @return the attribute bindings for this shader program.
     */
    public Map<String, VertexAttribute> getAttributeBindings() {
        return Collections.unmodifiableMap(attribBindings);
    }


    /**
     * Returns the active attribute associated with the given name.
     * Attributes are active if they are used in the actual shader program.
     *
     * @param name the name of the attribute to return.
     * @return the (active) {@code VertexAttribute} associated with the given name or {@code null} if no such attribute
     * exists.
     */
    public VertexAttribute getActiveAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Returns all active attribute bindings for this shader program.
     * Attributes are active if they are used in the actual shader program.
     *
     * @return all active attributes and their names as map from name to attribute.
     */
    public Map<String, VertexAttribute> getActiveAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Checks if the given name is associated with an active {@code VertexAttribute}.
     * Attributes are active if they are used in the actual shader program.
     *
     * @param name the name of the attribute to check.
     * @return {@code true} if the attribute associated with the given name is active.
     */
    public boolean isAttributeActive(String name) {
        return attributes.containsKey(name);
    }


    /**
     * Returns the (active) uniform associated with the given name.
     * Uniforms are active if they are used in the actual shader program.
     *
     * @param name the name of the {@code Uniform} to retrieve.
     * @return the active {@code Uniform} variable bound by the given name or {@code null} if no such binding exists.
     */
    public Uniform<?> getUniform(String name) {
        UniformBinding binding = uniforms.get(name);
        return binding != null ? binding.var : null;
    }

    /**
     * Returns all (active) uniform variable bindings.
     * Uniforms are active if they are used in the actual shader program.
     *
     * @return all active {@code Uniform} bindings.
     */
    public HashMap<String, Uniform<?>> getActiveUniforms() {
        HashMap<String, Uniform<?>> active = new HashMap<>();

        for (UniformBinding b : uniforms.values())
            active.put(b.var.getName(), b.var);

        return active;
    }

    /**
     * Return the OpenGL/GLSL location of the uniform variable associated with the given name.
     *
     * @param name the name of the uniform variable to get the location for.
     * @return the location of the uniform variable associated with the given name or {@code -1}, if no such variable
     * exists.
     */
    public int getUniformLocation(String name) {
        UniformBinding binding = uniforms.get(name);
        return binding != null ? binding.location : -1;
    }


    /**
     * Reloads all active attributes of this shader program.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    private void reloadActiveAttributes(GL2ES2 gl) {
        attributes.clear();

        // get number of active attributes
        int[] total = {-1};
        gl.glGetProgramiv(handle, GL2ES2.GL_ACTIVE_ATTRIBUTES, total, 0);

        // iterate over uniforms
        for (int i = 0; i < total[0]; i++) {
            byte[] namebuffer = new byte[256];

            int[] len    = {-1};
            int[] size   = {-1};
            int[] typeid = {-1};

            gl.glGetActiveAttrib(handle, i, namebuffer.length, len, 0, size, 0, typeid, 0, namebuffer, 0);
            String name = new String(namebuffer, 0, len[0]);

            int index = gl.glGetAttribLocation(handle, name);

            DataType        type   = new DataType(typeid[0], size[0]);
            VertexAttribute attrib = attribBindings.get(name);

            if (attrib == null) attrib = context.getVertexAttribManager().getDefaultAttributeBinding(name);

            if (attrib == null) {
                attrib = new VertexAttribute(type, index);
            } else if (DEBUG_CORE_VIS) {
                if (!type.equals(attrib.type)) {
                    logger.error("On ShaderProgram '" + this.name + "', Vertex-Attribute '" + name + "':"
                                 + "\n\tType specified by bindings differs from type specified in shader:"
                                 + " expected '" + attrib.type.getTypeString()
                                 + "', got '" + type.getTypeString() + "'.");
                }
            }

            attributes.put(name, attrib);
        }
    }

    /**
     * Reloads all active uniform variables of this shader program.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    private void reloadActiveUniforms(GL2ES2 gl) {
        HashMap<String, UniformBinding> uniforms = new HashMap<>();

        // get number of active uniforms
        int[] total = {-1};
        gl.glGetProgramiv(handle, GL2ES2.GL_ACTIVE_UNIFORMS, total, 0);

        // iterate over uniforms
        for (int i = 0; i < total[0]; i++) {
            byte[] namebuffer = new byte[256];

            int[] len  = {-1};
            int[] size = {-1};
            int[] type = {-1};

            // get uniform type and name
            gl.glGetActiveUniform(handle, i, namebuffer.length, len, 0, size, 0, type, 0, namebuffer, 0);
            String name = new String(namebuffer, 0, len[0]);

            // get uniform location
            int location = gl.glGetUniformLocation(handle, name);

            Uniform<?> uniform;
            DataType   utype = new DataType(type[0], size[0]);

            // check if uniform has changed
            UniformBinding binding = this.uniforms.get(name);
            if (binding != null && utype.equals(binding.var.getType())) {
                uniform = binding.var;
            } else {
                uniform = context.getUniformManager().create(name, utype);
                if (uniform != null) uniform.addOwner(this);
            }

            /*
             * The null-check is necessary here because some Vendors (nVidia) apparently implement const arrays
             * as uniform arrays (see http://stackoverflow.com/questions/21859433/glsl-const-array-pros).
             * This means there could be non-user specified uniforms of unknown/unsupported type.
             */
            if (uniform != null) uniforms.put(name, new UniformBinding(location, uniform));
        }

        // if uniform has changed, remove this program as owner
        for (UniformBinding binding : this.uniforms.values())
            if (!uniforms.containsKey(binding.var.getName())) binding.var.removeOwner(this);

        this.uniforms = uniforms;
    }


    /**
     * Binds this shader program and, if necessary, updates all dirty uniform variables.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    public void bind(GL2ES2 gl) {
        ShaderProgram current = context.ShaderState.getCurrentProgram();
        if (this == current) return;

        context.ShaderState.setCurrentProgram(this);
        bound = gl;

        gl.glUseProgram(handle);

        // update all dirty uniforms
        for (UniformBinding binding : uniforms.values()) {
            if (binding.dirty) {
                binding.var.update(gl, binding.location);
                binding.dirty = false;
            }
        }
    }

    /**
     * Unbinds the shader program.
     *
     * @param gl the {@code GL2ES2}-Object of the OpenGL context.
     */
    public void unbind(GL2ES2 gl) {
        bound = null;
        context.ShaderState.setCurrentProgram(null);
    }


    /**
     * Checks if this shader program is out-of-date and should to be re-linked.
     *
     * @return {@code true} if this shader program is out-of-date.
     */
    public boolean isDirty() {
        return dirty;
    }


    /**
     * Notifies this shader program that the value of the given {@code Uniform} has changed and needs to be updated.
     *
     * @param uniform the {@code Uniform} of which the value has changed.
     */
    public void uniformValueChanged(Uniform<?> uniform) {
        UniformBinding binding = uniforms.get(uniform.getName());

        // if program is bound, update directly
        if (bound != null)
            binding.var.update(bound, binding.location);
        else
            binding.dirty = true;
    }


    /**
     * Adds an observer that is being notified when the life-time of the wrapped OpenGL shader program ends, i.e.
     * a {@link ShaderProgram#dispose(GL2ES2)} call has been made.
     *
     * @param lto the {@code LifeTimeObserver} to add.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    public boolean addLifeTimeObserver(LifeTimeObserver<ShaderProgram> lto) {
        return ltObservers.add(lto);
    }

    /**
     * Removes the specified {@code LifeTimeObserver} from the set of observers for this shader program.
     *
     * @param lto the {@code LifeTimeObserver} to remove.
     * @return {@code true} if the underlying set of observers has changed by this call.
     */
    public boolean removeLifeTimeObserver(LifeTimeObserver<ShaderProgram> lto) {
        return ltObservers.remove(lto);
    }

    /**
     * Returns all {@code LifeTimeObservers} observing the life time of this shader program.
     *
     * @return all {@code LifeTimeObservers} observing the life time of this shader program.
     */
    public Set<LifeTimeObserver<ShaderProgram>> getLifeTimeObservers() {
        return Collections.unmodifiableSet(ltObservers);
    }


    /**
     * Uniform variable binding, binding a OpenGL/GLSL location to a uniform variable and indicating the state of
     * this binding (dirty meaning a need of the uniform to be updated).
     */
    protected static class UniformBinding {
        protected final int        location;
        protected final Uniform<?> var;
        protected boolean          dirty;

        /**
         * Constructs a new {@code UniformBinding} for the given location and {@code Uniform}. {@code dirty} is set
         * to {@code false}.
         *
         * @param location the OpenGL/GLSL location of the uniform variable.
         * @param var      the {@code Uniform} to bind the location to.
         */
        protected UniformBinding(int location, Uniform<?> var) {
            this.location = location;
            this.var      = var;
            this.dirty    = false;
        }
    }
}
