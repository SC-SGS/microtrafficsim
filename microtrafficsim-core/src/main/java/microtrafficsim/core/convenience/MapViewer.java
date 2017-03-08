package microtrafficsim.core.convenience;

import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;


/**
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public interface MapViewer {

    VisualizationPanel getVisualizationPanel();

    /**
     * @return the projection used in this map viewer
     */
    Projection getProjection();

    /**
     * @return initial window width
     */
    int getInitialWindowWidth();

    /**
     * @return initial window width
     */
    int getInitialWindowHeight();

    void addOverlay(int index, Overlay overlay);

    void addKeyCommand(short event, short vk, KeyCommand command);

    /**
     * Set up this example. Layer definitions describe the visual layers
     * to be rendered and are used to create a layer provider. With this
     * provider a tile provider is created, capable of returning drawable
     * tiles. These tiles are rendered using the visualization object. A
     * parser is created to parse the specified file (asynchronously) and
     * update the layers (respectively their sources).
     */
    default void show() {
        getVisualizationPanel().start();
    }

    default void stop() {
        getVisualizationPanel().stop();
    }

    default void destroy() {
        VisualizationPanel panel = getVisualizationPanel();
        panel.stop();
        panel.getParent().remove(panel);
        panel.destroy();
    }

    /**
     * @throws UnsupportedFeatureException if not all required OpenGL features
     *                                     are available
     */
    default void create() throws UnsupportedFeatureException {
        create(null);
    }

    /**
     * @throws UnsupportedFeatureException if not all required OpenGL features
     *                                     are available
     */
    void create(ScenarioConfig config) throws UnsupportedFeatureException;

    /**
     * Create the (tile-based) visualization object. The visualization object is
     * used to bind input and display structures together.
     */
    void createVisualization();

    /**
     * Create a visualization panel. The visualization panel is the interface
     * between the Java UI toolkit (Swing) and the OpenGL-based visualization.
     * Thus it is used to display the visualization.
     *
     * @throws UnsupportedFeatureException if not all required OpenGL features
     *                                     are available
     */
    void createVisualizationPanel() throws UnsupportedFeatureException;

    void setMap(SegmentFeatureProvider segment) throws InterruptedException;
}