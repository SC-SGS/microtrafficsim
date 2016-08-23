package microtrafficsim.core.map.style;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.Collection;


// TODO: comments

/**
 * Stylesheet for the tile-based visualization.
 *
 * @author Maximilian Luz
 */
public interface StyleSheet {

    /**
     * Returns the background-color of this style-sheet.
     *
     * @return the background-color of this style-sheet.
     */
    Color getBackgroundColor();

    /**
     * Returns the background-color for tiles of this style.
     *
     * @return the background-color for tiles of this style.
     */
    Color getTileBackgroundColor();

    /**
     * Returns the parser-configuration of this style.
     *
     * @return the parser-configuration of this style.
     */
    ParserConfig getParserConfiguration();

    /**
     * Returns the map feature-definitions of this style.
     *
     * @return the map feature-definitions of this style.
     */
    Collection<MapFeatureDefinition<?>> getFeatureDefinitions();

    /**
     * Returns the layer definitions of this style.
     *
     * @return the layer definitions of this style.
     */
    Collection<LayerDefinition> getLayers();

    /**
     * Parser configuration for style.
     */
    class ParserConfig {

        /**
         * The generator-index marking the unification-process in the sequence of generators.
         */
        public final int generatorIndexOfUnification;

        /**
         * The generator-index marking the street-graph generation-process in the sequence of generators.
         */
        public final int generatorIndexOfStreetGraph;

        /**
         * Constructs a new {@code ParserConfig}.
         *
         * @param generatorIndexOfUnification the generator-index marking the unification-process in the sequence of
         *                                    generators.
         * @param generatorIndexOfStreetGraph the generator-index marking the street-graph generation-process in the
         *                                    sequence of generators.
         */
        public ParserConfig(int generatorIndexOfUnification, int generatorIndexOfStreetGraph) {
            this.generatorIndexOfUnification = generatorIndexOfUnification;
            this.generatorIndexOfStreetGraph = generatorIndexOfStreetGraph;
        }
    }
}
