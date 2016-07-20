package microtrafficsim.core.map.style;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.MapFeatureDefinition;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.Collection;


// TODO: comments

/**
 * Stylesheet for the tile-based visualization.
 *
 * @author Maximilian Luz
 */
public interface StyleSheet {

    Color getBackgroundColor();
    Color getTileBackgroundColor();

    ParserConfig getParserConfiguration();

    Collection<MapFeatureDefinition<?>> getFeatureDefinitions();
    Collection<LayerDefinition>     getLayers();

    /**
     * Parser configuration for style.
     */
    class ParserConfig {
        public final int generatorIndexOfUnification;
        public final int generatorIndexOfStreetGraph;

        public ParserConfig(int generatorIndexOfUnification, int generatorIndexOfStreetGraph) {
            this.generatorIndexOfUnification = generatorIndexOfUnification;
            this.generatorIndexOfStreetGraph = generatorIndexOfStreetGraph;
        }
    }
}
