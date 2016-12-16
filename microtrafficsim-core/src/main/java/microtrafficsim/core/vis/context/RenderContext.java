package microtrafficsim.core.vis.context;

import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import microtrafficsim.core.vis.Renderer;
import microtrafficsim.core.vis.context.exceptions.UncaughtContextException;
import microtrafficsim.core.vis.context.exceptions.UncaughtExceptionHandler;
import microtrafficsim.core.vis.context.state.*;
import microtrafficsim.core.vis.context.tasks.FutureRenderTask;
import microtrafficsim.core.vis.context.tasks.RenderTask;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import static microtrafficsim.build.BuildSetup.DEBUG_CORE_VIS;


/**
 * Context representing the state of an OpenGL context.
 *
 * @author Maximilian Luz
 */
public class RenderContext implements GLEventListener {
    private static final Logger logger = new EasyMarkableLogger(RenderContext.class);

    /**
     * Execution budget per frame in nanoseconds for render-tasks.
     */
    private static final long RTASK_EXECUTION_BUDGED_NS = 33_000_000;


    // -- state ---------------------------------------------------------------
    public final ViewportState    Viewport;
    public final ClearColor       ClearColor;
    public final ClearDepth       ClearDepth;
    public final DepthTest        DepthTest;
    public final BlendMode        BlendMode;
    public final PrimitiveRestart PrimitiveRestart;
    public final PointState       Points;
    public final ShaderState      ShaderState;

    private Renderer renderer;


    // -- context -------------------------------------------------------------
    private Queue<FutureRenderTask<?>> tasks;
    private GLAnimatorControl          animator;
    private GLAutoDrawable             drawable;
    private UncaughtExceptionHandler   exhdlr;
    private ShaderManager              shaders;
    private UniformManager             uniforms;
    private VertexAttributeManager     attributes;

    {
        Viewport         = new ViewportState();
        ClearColor       = new ClearColor();
        ClearDepth       = new ClearDepth();
        DepthTest        = new DepthTest();
        BlendMode        = new BlendMode();
        PrimitiveRestart = new PrimitiveRestart();
        Points           = new PointState();
        ShaderState      = new ShaderState();
    }


    /**
     * Constructs a new {@code RenderContext}. This does not create an actual OpenGL context.
     */
    public RenderContext() {
        this.tasks = new ConcurrentLinkedQueue<>();

        this.renderer = null;
        this.animator = null;
        this.drawable = null;
        this.exhdlr   = null;

        this.shaders    = new ShaderManager();
        this.uniforms   = new UniformManager();
        this.attributes = new VertexAttributeManager();
    }

    /**
     * Returns the renderer called by this context.
     *
     * @return the renderer called by this context.
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Sets the renderer that is going to be called by this context.
     *
     * @param renderer the new renderer that is going to be called by this context.
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Returns the {@code ShaderManager} of this context.
     *
     * @return the {@code ShaderManager} of this context.
     */
    public ShaderManager getShaderManager() {
        return shaders;
    }

    /**
     * Returns the {@code UniformManager} of this context.
     *
     * @return the {@code UniformManager} of this context.
     */
    public UniformManager getUniformManager() {
        return uniforms;
    }

    /**
     * Returns the {@code VertexAttributeManager} of this context.
     *
     * @return the {@code VertexAttributeManager} of this context.
     */
    public VertexAttributeManager getVertexAttribManager() {
        return attributes;
    }


    /**
     * Returns the task-queue used to perform synchronized tasks on the OpenGL context.
     *
     * @return the task-queue used to perform synchronized tasks on the OpenGL context.
     */
    public Queue<? extends Future<?>> getTaskQueue() {
        return tasks;
    }

    /**
     * Adds the given task to the task-queue of this context. The task-queue is used to
     * perform synchronized tasks on the OpenGL context.
     *
     * @param task  the task to be executed.
     * @param delay set to {@code true} to delay the task if the OpenGL context is current. If set to {@code false} and
     *              the OpenGL context is current, the task is going to be executed directly without waiting for the
     *              next frame.
     * @param <V>   the return-type of the submitted task.
     * @return the {@code Future} corresponding to the submitted task.
     */
    public <V> Future<V> addTask(RenderTask<V> task, boolean delay) {
        FutureRenderTask<V> future = new FutureRenderTask<>(task);

        // if delay is false and the context is current on this thread, run the task instantly
        GLAutoDrawable drawable = this.drawable;
        if (!delay && drawable != null && drawable.getContext().isCurrent())
            future.run(this);
        else
            tasks.add(future);

        return future;
    }

    /**
     * Adds the given task to the task-queue of this context. The task-queue is used to
     * perform synchronized tasks on the OpenGL context.
     * <p>
     * This call is equal to {@link RenderContext#addTask(RenderTask, boolean) addTask(task, false)}
     * </p>
     *
     * @param task the task to be executed.
     * @param <V>  the return-type of the submitted task.
     * @return the {@code Future} corresponding to the submitted task.
     */
    public <V> Future<V> addTask(RenderTask<V> task) {
        return addTask(task, false);
    }

    /**
     * Checks if there are any tasks to be executed on this context.
     *
     * @return {@code true} if there are any tasks that should be executed on this context.
     */
    public boolean hasTasks() {
        return tasks.isEmpty();
    }


    /**
     * Return the animator used for this context.
     *
     * @return the {@code GLAnimatorControl} used for this context.
     */
    public GLAnimatorControl getAnimator() {
        return animator;
    }

    /**
     * Set the animator used for this context.
     *
     * @param animator the new animator used for this context.
     */
    public void setAnimator(GLAnimatorControl animator) {
        this.animator = animator;
    }


    /**
     * Returns the {@code UncaughtExceptionHandler} used for this context.
     *
     * @return the {@code UncaughtExceptionHandler} used for this context.
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return exhdlr;
    }

    /**
     * Sets the {@code UncaughtExceptionHandler} used for this context.
     *
     * @param handler the {@code UncaughtExceptionHandler} to be used for this context.
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        this.exhdlr = handler;
    }


    /**
     * Returns the drawable that is active on this context.
     *
     * @return the drawable active on this context or {@code null} if no drawable is active.
     */
    public GLAutoDrawable getDrawable() {
        return drawable;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        this.drawable = drawable;
        this.Viewport.setInternal(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

        try {
            renderer.init(this);
        } catch (Throwable exception) {
            if (!handleException(exception)) throw new UncaughtContextException(this, exception);
        } finally { this.drawable = null; }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        this.drawable = drawable;
        this.Viewport.setInternal(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

        // finish executing all tasks (may contain cleanup tasks)
        while (!tasks.isEmpty()) {
            tasks.poll().run(this);
            Thread.interrupted();    // interrupts are task-local, clear if necessary
        }

        try {
            renderer.dispose(this);
        } catch (Throwable exception) {
            if (!handleException(exception)) throw new UncaughtContextException(this, exception);
        } finally { this.drawable = null; }

        shaders.dispose(drawable.getGL().getGL2ES2(), true);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        this.drawable = drawable;
        this.Viewport.setInternal(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

        // execute tasks on work queue
        long t = System.nanoTime();
        while (!tasks.isEmpty()) {
            tasks.poll().run(this);
            Thread.interrupted();    // interrupts are task-local, clear if necessary

            // make sure we do not block the main thread
            long dt = System.nanoTime() - t;
            if (dt > RTASK_EXECUTION_BUDGED_NS) {
                logger.warn("time for combined task execution exceeded threshold: " + (dt / 1_000_000) + "ms");
                break;
            }
        }

        // display renderer
        try {
            renderer.display(this);
        } catch (Throwable exception) {
            if (!handleException(exception)) throw new UncaughtContextException(this, exception);
        } finally { this.drawable = null; }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.drawable = drawable;
        this.Viewport.setInternal(0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

        try {
            renderer.reshape(this, x, y, width, height);
        } catch (Throwable exception) {
            if (!handleException(exception)) throw new UncaughtContextException(this, exception);
        } finally { this.drawable = null; }
    }


    /**
     * Handle the previously uncaught exception.
     *
     * @param exception the uncaught exception.
     * @return {@code true} if the exception has been handled by this function.
     */
    private boolean handleException(Throwable exception) {
        if (animator != null) animator.stop();

        if (exhdlr != null) {
            try {
                exhdlr.uncaughtException(this, exception);
            } catch (Throwable t) {
                /*
                 * Any Exceptions thrown by the exception-handler are ignored,
                 * we are already handling an exception.
                 */

                if (DEBUG_CORE_VIS)
                    logger.debug("Ignoring Exception from ExceptionHandler:", t);
            }
            return true;
        }
        return false;
    }
}
