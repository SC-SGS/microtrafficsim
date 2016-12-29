package microtrafficsim.core.mapviewer;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;

import java.io.File;
import java.util.Collection;


/**
 * @author Dominic Parga Cacheiro
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
    void create(SimulationConfig config) throws UnsupportedFeatureException;

    /**
     * Set up this example. Layer definitions describe the visual layers
     * to be rendered and are used to create a layer provider. With this
     * provider a tile provider is created, capable of returning drawable
     * tiles. These tiles are rendered using the visualization object. A
     * parser is created to parse the specified file (asynchronously) and
     * update the layers (respectively their sources).
     */
    void show();

    void stop();

    /**
     * Create the (tile-based) visualization object. The visualization object is
     * used to bind input and display structures together.
     *
     * @param provider the provider providing the tiles to be displayed
     * @return the created visualization object
     */
    TileBasedVisualization createVisualization(TileProvider provider);

    /**
     * Create a visualization panel. The visualization panel is the interface
     * between the Java UI toolkit (Swing) and the OpenGL-based visualization.
     * Thus it is used to display the visualization.
     *
     * @param vis the visualization to show on the panel
     * @return the created visualization panel
     * @throws UnsupportedFeatureException if not all required OpenGL features
     *                                     are available
     */
    VisualizationPanel createVisualizationPanel(TileBasedVisualization vis) throws UnsupportedFeatureException;

    void addKeyCommand(short event, short vk, KeyCommand command);

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
     *
     * @return the created parser
     */
    default OSMParser createParser() {
        return createParser(null);
    }

    OSMParser createParser(SimulationConfig simconfig);

    /**
     * Creates a {@code TileLayerProvider} from the given layer definitions.
     * The {@code TileLayerProvider} is used to provide map-layers and their
     * style to the visualization. {@code LayerDefinition}s describe such
     * a layer in dependence of a source object. {@code TileLayerGenerator}s
     * are used to generate a renderable {@code TileLayer} from a specified
     * source.
     *
     * @param layers the layer definitions for the provider
     * @return the created layer provider
     */
    TileLayerProvider createLayerProvider(Collection<LayerDefinition> layers);

    void changeMap(OSMParser.Result result) throws InterruptedException;

    OSMParser.Result parse(File file) throws Exception;
}
