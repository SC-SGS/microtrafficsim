package microtrafficsim.core.vis;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;


/**
 * The panel displaying the {@code Visualization} using a separated render-thread.
 *
 * @author Maximilian Luz
 */
public class VisualizationPanel extends JPanel {
    private static final long serialVersionUID = -3494671834324453286L;

    private GLWindow window;
    private NewtCanvasAWT canvas;
    private FPSAnimator animator;
    private Visualization visualization;


    /**
     * Constructs a new visualization-panel using the default configuration. This constructor is equal to
     * {@link
     *  VisualizationPanel#VisualizationPanel(Visualization, VisualizerConfig)
     *  VisualizationPanel(visualization, visualization.getDefaultConfig())
     * }
     *
     * @param visualization the visualization to be displayed.
     * @throws UnsupportedFeatureException if the system does not support one or more required OpenGL features.
     */
    public VisualizationPanel(Visualization visualization) throws UnsupportedFeatureException {
        this(visualization, visualization.getDefaultConfig());
    }

    /**
     * Constructs a new visualization-panel using the specified configuration.
     *
     * @param visualization the visualization to be displayed.
     * @param config        the configuration for the visualization-panel to be created.
     */
    public VisualizationPanel(Visualization visualization, VisualizerConfig config) {
        this.visualization = visualization;

        window = GLWindow.create(config.glcapabilities);
        window.addGLEventListener(visualization.getRenderContext());
        window.addMouseListener(visualization.getMouseListener());
        window.addKeyListener(visualization.getKeyController());

        animator = new FPSAnimator(window, config.fps, true);
        visualization.getRenderContext().setAnimator(animator);

        canvas = new NewtCanvasAWT(window);

        this.setLayout(new BorderLayout());
        this.add(canvas, BorderLayout.CENTER);
    }


    /**
     * Start the render-thread.
     */
    public void start() {
        animator.start();
    }

    /**
     * Pause the render-thread.
     */
    public void pause() {
        animator.pause();
    }

    /**
     * Stop the render-thread.
     */
    public void stop() {
        animator.stop();
    }

    public void destroy() {
        canvas.destroy();
    }

    /**
     * Checks if the render-thread has been started.
     *
     * @return {@code true} if the render-thread has been started.
     */
    public boolean isStarted() {
        return animator.isStarted();
    }

    /**
     * Checks if the render-thread has been paused.
     *
     * @return {@code true} if the render-thread has been paused.
     */
    public boolean isPaused() {
        return animator.isPaused();
    }


    /**
     * Returns the visualization being displayed in this panel.
     *
     * @return the visualization being displayed in this panel.
     */
    public Visualization getVisualization() {
        return visualization;
    }
}
