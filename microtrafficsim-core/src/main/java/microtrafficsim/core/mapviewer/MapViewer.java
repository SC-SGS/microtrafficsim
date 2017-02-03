package microtrafficsim.core.mapviewer;

import com.jogamp.newt.util.EDTUtil;
import jogamp.newt.DefaultEDTUtil;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.Visualization;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;

import java.io.File;


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

    /**
     * @see #createParser(ScenarioConfig)
     */
    default void createParser() {
        createParser(null);
    }

    /**
     * Creates and sets up the parser used to parse OSM files. The parser
     * processes the file in two major steps: first a simple extraction,
     * based on the feature set requested (using {@code putMapFeatureDefinition},
     * and second a transformation of the extracted data to the final
     * representation, using the {@code FeatureGenerator}s associated with
     * the feature definition. {@code FeatureGenerator}s may require the
     * presence of certain components for ways and nodes (such as the
     * {@code StreetComponent}), or factories to initialize relations
     * (such as the {@code RestrictionRelationFactory}). The in this example
     * provided components and initializers are enough for most use-cases.
     */
    void createParser(ScenarioConfig simconfig);

    void changeMap(OSMParser.Result result) throws InterruptedException;

    OSMParser.Result parse(File file) throws Exception;
}