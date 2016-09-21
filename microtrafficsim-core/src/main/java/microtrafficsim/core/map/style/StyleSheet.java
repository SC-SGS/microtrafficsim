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

    /**
     * Placeholder {@code FeatureDefinition} that can be used to specify dependencies on the street-graph-generation.
     */
    FeatureDefinition DEPENDS_ON_STREETGRAPH
            = FeatureDefinition.createDependencyPlaceholder(
                    "placeholder for streetgraph feature-definitnion (stylesheet)");

    /**
     * Placeholder {@code FeatureDefinition} that can be used to specify dependencies on the street-unification step.
     */
    FeatureDefinition DEPENDS_ON_UNIFICATION
            = FeatureDefinition.createDependencyPlaceholder(
                    "placeholder for unification feature-definitnion (stylesheet)");


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


    /**
     * Replace the placeholder-{@code FeatureDefinition}s of this {@code StyleSheet} with the specified
     * {@code FeatureDefinition}s.
     *
     * @param unification the definition to replace the unification-step-placeholder with.
     * @param streetgraph the definition to replace the streetgraph-placeholder with.
     */
    default void replaceDependencyPlaceholders(FeatureDefinition unification, FeatureDefinition streetgraph) {
        for (MapFeatureDefinition<?> def : getFeatureDefinitions()) {
            if (def.getDependency().getRequires().remove(DEPENDS_ON_STREETGRAPH))
                def.getDependency().getRequires().add(streetgraph);

            if (def.getDependency().getRequires().remove(DEPENDS_ON_UNIFICATION))
                def.getDependency().getRequires().add(unification);

            if (def.getDependency().getRequiredBy().remove(DEPENDS_ON_STREETGRAPH))
                def.getDependency().getRequiredBy().add(streetgraph);

            if (def.getDependency().getRequiredBy().remove(DEPENDS_ON_UNIFICATION))
                def.getDependency().getRequiredBy().add(unification);
        }
    }
}
