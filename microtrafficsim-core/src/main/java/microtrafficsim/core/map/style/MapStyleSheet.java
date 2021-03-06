package microtrafficsim.core.map.style;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.osm.parser.features.FeatureDefinition;

import java.util.Collection;


/**
 * Stylesheet for the tile-based visualization.
 *
 * @author Maximilian Luz
 */
public interface MapStyleSheet {

    /*
    |============|
    | map colors |
    |============|
    */
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

    /*
    |=====================|
    | features and layers |
    |=====================|
    */
    /**
     * Placeholder {@code FeatureDefinition} that can be used to specify dependencies on the street-unification step.
     */
    FeatureDefinition DEPENDS_ON_WAY_CLIPPING
            = FeatureDefinition.createDependencyPlaceholder(
            "placeholder for way-clipping stage (stylesheet)");

    /**
     * Placeholder {@code FeatureDefinition} that can be used to specify dependencies on the street-unification step.
     */
    FeatureDefinition DEPENDS_ON_UNIFICATION
            = FeatureDefinition.createDependencyPlaceholder(
            "placeholder for street-unification stage (stylesheet)");

    /**
     * Placeholder {@code FeatureDefinition} that can be used to specify dependencies on the street-graph-generation.
     */
    FeatureDefinition DEPENDS_ON_STREETGRAPH
            = FeatureDefinition.createDependencyPlaceholder(
            "placeholder for streetgraph feature-definitnion (stylesheet)");

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
     * Return the width of one lane for the specified street-type and zoom-level.
     *
     * @param zoom the zoom-level for which this property should be queried.
     * @return the width of one lane for the specified arguments. The complete width of a street is calculated as:
     * {@code (lanesForward + lanesBackward) * laneWidth} for the inline, and
     * {@code (lanesForward + lanesBackward) * laneWidth + 2 * outlineWidth} for the outline.
     */
    double getStreetLaneWidth(int zoom);

    /**
     * Return the normalized width of one lane for the specified street-type and zoom-level.
     *
     * @param zoom the zoom-level for which this property should be queried.
     * @return the width of one lane for the specified arguments. The complete width of a street is calculated as:
     * {@code (lanesForward + lanesBackward) * laneWidth} for the inline, and
     * {@code (lanesForward + lanesBackward) * laneWidth + 2 * outlineWidth} for the outline.
     */
    default double getNormalizedStreetLaneWidth(int zoom) {
        return getScaleNorm() * getStreetLaneWidth(zoom);
    }

    /**
     * Returns the scale normalization factor, used to calculate the real lane width (amongst other).
     *
     * @return the scale normalization factor, used to calculate the real lane width (amongst other).
     */
    double getScaleNorm();


    /**
     * Replace the placeholder-{@code FeatureDefinition}s of this {@code StyleSheet} with the specified
     * {@code FeatureDefinition}s.
     *
     * @param clipping    the definition to replace the way-clipping-step-placeholder with.
     * @param unification the definition to replace the unification-step-placeholder with.
     * @param streetgraph the definition to replace the streetgraph-placeholder with.
     */
    default void replaceDependencyPlaceholders(FeatureDefinition clipping, FeatureDefinition unification,
                                               FeatureDefinition streetgraph) {
        for (MapFeatureDefinition<?> def : getFeatureDefinitions()) {
            if (def.getDependency().getRequires().remove(DEPENDS_ON_WAY_CLIPPING))
                def.getDependency().getRequires().add(clipping);

            if (def.getDependency().getRequires().remove(DEPENDS_ON_UNIFICATION))
                def.getDependency().getRequires().add(unification);

            if (def.getDependency().getRequires().remove(DEPENDS_ON_STREETGRAPH))
                def.getDependency().getRequires().add(streetgraph);

            if (def.getDependency().getRequiredBy().remove(DEPENDS_ON_WAY_CLIPPING))
                def.getDependency().getRequiredBy().add(clipping);

            if (def.getDependency().getRequiredBy().remove(DEPENDS_ON_UNIFICATION))
                def.getDependency().getRequiredBy().add(unification);

            if (def.getDependency().getRequiredBy().remove(DEPENDS_ON_STREETGRAPH))
                def.getDependency().getRequiredBy().add(streetgraph);
        }
    }
}
