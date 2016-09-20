package microtrafficsim.core.map.style;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureDependency;

import java.util.Collection;
import java.util.function.UnaryOperator;


/**
 * Stylesheet for the tile-based visualization.
 *
 * @author Maximilian Luz
 */
public interface StyleSheet {
    FeatureDefinition DEPENDS_ON_STREETGRAPH = FeatureDefinition.createDependencyPlaceholder();
    FeatureDefinition DEPENDS_ON_UNIFICATION = FeatureDefinition.createDependencyPlaceholder();


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
     * Returns the feature-definitions of this style.
     *
     * @return the feature-definitions of this style.
     */
    Collection<MapFeatureDefinition<?>> getFeatureDefinitions();

    /**
     * Returns the layer definitions of this style.
     *
     * @return the layer definitions of this style.
     */
    Collection<LayerDefinition> getLayers();


    default void replaceDependencyPlaceholders(FeatureDefinition unification, FeatureDefinition streetgraph) {
        UnaryOperator<FeatureDefinition> replacefn = x -> {
            if (x == DEPENDS_ON_STREETGRAPH)
                return streetgraph;
            else if (x == DEPENDS_ON_UNIFICATION)
                return unification;
            else
                return x;
        };

        for (MapFeatureDefinition<?> def : getFeatureDefinitions()) {
            def.getDependency().getRequires().replaceAll(replacefn);
            def.getDependency().getRequiredBy().replaceAll(replacefn);
        }
    }
}
