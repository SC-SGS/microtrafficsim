package microtrafficsim.core.map.style;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.style.impl.DarkMonochromeStyleSheet;
import microtrafficsim.core.map.style.predicates.BasicStreetBasePredicate;
import microtrafficsim.core.map.style.predicates.MajorStreetBasePredicate;
import microtrafficsim.core.map.style.predicates.StreetBasePredicate;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.parser.features.MapFeatureGenerator;
import microtrafficsim.core.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.mesh.StreetMeshGenerator;
import microtrafficsim.core.vis.mesh.builder.LineMeshBuilder;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;


/**
 * A basic style-sheet for the MapViewer. It implements basic functionality, so the sub-classes does only have to
 * care for definitions like colors.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public abstract class BasicStyleSheet implements StyleSheet {
    private final EasyMarkableLogger logger = new EasyMarkableLogger(BasicStyleSheet.class);

    private static final double SCALE_MAXLEVEL = 1.0 / (2 << 19);

    protected ArrayList<MapFeatureDefinition<?>> features = new ArrayList<>();
    protected ArrayList<LayerDefinition>         layers = new ArrayList<>();


    /**
     * Creates a new BasicStyleSheet by calling {@link #initialize()}.
     */
    protected BasicStyleSheet() {
        initialize();
    }


    @Override
    public Collection<MapFeatureDefinition<?>> getFeatureDefinitions() {
        return features;
    }

    @Override
    public Collection<LayerDefinition> getLayers() {
        return layers;
    }


    /**
     * Initializes this style-sheet.
     */
    protected void initialize() {
        /* get the streets to be displayed */
        StreetBasePredicate[] streets = getStreetPredicates();

        /* define and add the features */
        MapFeatureGenerator<Street> generator = new StreetFeatureGenerator();

        for (StreetBasePredicate street : streets)
            features.add(genStreetFeatureDef("streets:" + street.getType(), generator, street));

        /* styles and layers */
        ShaderProgramSource shader = getStreetShader();

        for (int zoom = 19; zoom >= 0; zoom--) {
            for (StreetBasePredicate street : streets) {
                String streetType = street.getType();
                String featureName = "streets:" + streetType;

                if (isStreetOutlineActive(streetType, zoom)) {
                    Style style = genStreetBaseStyle(
                            shader,
                            getStreetOutlineColor(streetType),
                            getStreetLaneWidth(zoom),
                            getStreetOutlineWidth(streetType, zoom),
                            SCALE_MAXLEVEL);
                    layers.add(genLayer(featureName + ":outline:" + zoom, layers.size(), zoom, zoom, featureName, style));
                }
            }

            for (StreetBasePredicate street : streets) {
                String streetType = street.getType();
                String featureName = "streets:" + streetType;

                if (isStreetInlineActive(streetType, zoom)) {
                    Style style = genStreetBaseStyle(
                            shader,
                            getStreetInlineColor(streetType),
                            getStreetLaneWidth(zoom),
                            0.0f,
                            SCALE_MAXLEVEL);
                    layers.add(genLayer(featureName + ":inline:" + zoom, layers.size(), zoom, zoom, featureName, style));
                }
            }

            for (StreetBasePredicate street : streets) {        // TODO: selectors, color, line-width
                String streetType = street.getType();
                String featureName = "streets:" + streetType;

                if (isStreetInlineActive(streetType, zoom)) {
                    Style style = genStreetLinesStyle(
                            shader,
                            Color.fromRGB(0x000000),
                            getStreetLaneWidth(zoom),
                            5.0f,
                            SCALE_MAXLEVEL,
                            true);
                    layers.add(genLayer(featureName + ":lines-center:" + zoom, layers.size(), zoom, zoom, featureName, style));
                }

                if (isStreetInlineActive(streetType, zoom)) {
                    Style style = genStreetLinesStyle(
                            shader,
                            Color.fromRGB(0x000000),
                            getStreetLaneWidth(zoom),
                            4.0f,
                            SCALE_MAXLEVEL,
                            false);
                    layers.add(genLayer(featureName + ":lines-outer:" + zoom, layers.size(), zoom, zoom, featureName, style));
                }
            }
        }
    }

    /**
     * Returns the predicates selecting the Streets displayed with this style-sheet.
     *
     * @return the predicates selecting the Streets displayed with this style-sheet. The feature-layers are generated
     * in the same order as returned in this function, meaning streets with a higher index in this array will be
     * displayed above streets with a lower index.
     */
    protected StreetBasePredicate[] getStreetPredicates() {
        return new StreetBasePredicate[] {
                new BasicStreetBasePredicate("living_street"),
                new BasicStreetBasePredicate("road"),
                new BasicStreetBasePredicate("residential"),
                new BasicStreetBasePredicate("unclassified"),
                new MajorStreetBasePredicate("tertiary"),
                new MajorStreetBasePredicate("secondary"),
                new MajorStreetBasePredicate("primary"),
                new MajorStreetBasePredicate("trunk"),
                new MajorStreetBasePredicate("motorway"),
        };
    }

    /**
     * Returns if the specified street-type should be displayed with an outline at the specified zoom-level.
     *
     * @param streetType the type of the street as specified in the StreetBasePredicate.
     * @param zoom the zoom-level for which this property should be queried.
     * @return {@code true} if an outline should be rendered for the given street-type at the given zoom-level.
     */
    protected boolean isStreetOutlineActive(String streetType, int zoom) {
        switch (streetType) {
            case "motorway":
                return true;
            case "trunk":
                return zoom >= 5;
            case "primary":
                return zoom >= 7;
            case "secondary":
            case "tertiary":
                return zoom >= 9;
            case "unclassified":
            case "residential":
            case "road":
                return zoom >= 12;
            case "living_street":
                return zoom >= 13;
            default:
                logger.info("It is not defined whether " + streetType + " has an active outline.");
                return false;
        }
    }

    /**
     * Returns if the specified street-type should be displayed with an inline at the specified zoom-level.
     *
     * @param streetType the type of the street as specified in the StreetBasePredicate.
     * @param zoom the zoom-level for which this property should be queried.
     * @return {@code true} if an inline should be rendered for the given street-type at the given zoom-level.
     */
    protected boolean isStreetInlineActive(String streetType, int zoom) {
        switch (streetType) {
            case "motorway":
                return true;
            case "trunk":
                return zoom >= 5;
            case "primary":
                return zoom >= 7;
            case "secondary":
            case "tertiary":
                return zoom >= 9;
            case "unclassified":
            case "residential":
            case "road":
                return zoom >= 12;
            case "living_street":
                return zoom >= 13;
            default:
                logger.info("It is not defined whether " + streetType + " has an active inline.");
                return false;
        }
    }


    /**
     * Return the outline-color for the specified street-type.
     *
     * @param streetType the type of the street as specified in the StreetBasePredicate.
     * @return the {@code Color} of the outline of the specified street-type.
     */
    protected abstract Color getStreetOutlineColor(String streetType);

    /**
     * Return the inline-color for the specified street-type.
     *
     * @param streetType the type of the street as specified in the StreetBasePredicate.
     * @return the {@code Color} of the inline of the specified street-type.
     */
    protected abstract Color getStreetInlineColor(String streetType);

    /**
     * Return the width-offset to be added to each side of the street (i.e. the outline)
     * @param streetType the type of the street as specified in the StreetBasePredicate.
     * @param zoom the zoom-level for which this property should be queried.
     * @return the width of the outline of a street for the specified properties.
     */
    protected abstract double getStreetOutlineWidth(String streetType, int zoom);

    @Override
    public double getScaleNorm() {
        return SCALE_MAXLEVEL;
    }

    /**
     * Generates a basic street-feature definition.
     *
     * @param name      the name of the feature.
     * @param generator the generator for the feature.
     * @param predicate the predicate to select the Ways contained in this feature.
     * @return the created MapFeatureDefinition.
     */
    protected MapFeatureDefinition<Street> genStreetFeatureDef(String name, MapFeatureGenerator<Street> generator,
                                                             Predicate<Way> predicate) {
        FeatureDependency dependency = new FeatureDependency();
        dependency.addRequires(DEPENDS_ON_WAY_CLIPPING);
        dependency.addRequires(DEPENDS_ON_UNIFICATION);
        dependency.addRequires(DEPENDS_ON_STREETGRAPH);

        return new MapFeatureDefinition<>(name, dependency, generator, n -> false, predicate);
    }

    /**
     * Return the source of the shader used for street-rendering.
     *
     * @return the created shader-sources.
     */
    protected ShaderProgramSource getStreetShader() {
        Resource vert = new PackagedResource(DarkMonochromeStyleSheet.class, "/shaders/features/streets/streets.vs");
        Resource frag = new PackagedResource(DarkMonochromeStyleSheet.class, "/shaders/features/streets/streets.fs");

        ShaderProgramSource prog = new ShaderProgramSource("streets");
        prog.addSource(GL3.GL_VERTEX_SHADER, vert);
        prog.addSource(GL3.GL_FRAGMENT_SHADER, frag);

        return prog;
    }

    /**
     * Generate a style for the base-lines of streets based on the specific properties.
     *
     * @param shader    the shader to be used in the generated style.
     * @param color     the color to be used in generated style.
     * @param lanewidth the width of a single lane of the generated style.
     * @param outline   the width of the outline of the generated style.
     * @param scalenorm the scale-normal of the generated style.
     * @return the generated style.
     */
    protected Style genStreetBaseStyle(ShaderProgramSource shader, Color color, double lanewidth, double outline,
                                       double scalenorm)
    {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
        style.setProperty("lanewidth", lanewidth * scalenorm);
        style.setProperty("linewidth", outline * scalenorm);
        style.setProperty("cap", LineMeshBuilder.CapType.ROUND);
        style.setProperty("join", LineMeshBuilder.JoinType.ROUND);
        style.setProperty("type", StreetMeshGenerator.LineType.BASE);
        style.setProperty("miter-angle-limit", 0.5);
        return style;
    }

    /**
     * Generate a style for streets based on the specific properties.
     *
     * @param shader    the shader to be used in the generated style.
     * @param color     the color to be used in generated style.
     * @param lanewidth the width of a single lane of the generated style.
     * @param linewidth the width of the lines of the generated style.
     * @param scalenorm the scale-normal of the generated style.
     * @return the generated style.
     */
    protected Style genStreetLinesStyle(ShaderProgramSource shader, Color color, double lanewidth, double linewidth,
                                       double scalenorm, boolean center)
    {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
        style.setProperty("lanewidth", lanewidth * scalenorm);
        style.setProperty("linewidth", linewidth * scalenorm);
        style.setProperty("cap", LineMeshBuilder.CapType.SQUARE);
        style.setProperty("join", LineMeshBuilder.JoinType.MITER);
        style.setProperty("type", center ? StreetMeshGenerator.LineType.LINES_CENTER : StreetMeshGenerator.LineType.LINES_OUTER);
        style.setProperty("miter-angle-limit", 0.5);
        return style;
    }

    /**
     * Generate a LayerDefinition for features, based on the specific properties.
     *
     * @param name    the name of the generated layer.
     * @param index   the index of the  generated layer.
     * @param min     the minimum zoom-level at which the generated layer is visible.
     * @param max     the maximum zoom-level at which the generated layer is visible.
     * @param feature the feature-name of the feature on which the layer is based on.
     * @param style   the style to be used for rendering.
     * @return the generated LayerDefinition.
     */
    protected LayerDefinition genLayer(String name, int index, int min, int max, String feature, Style style) {
        return new LayerDefinition(name, index, min, max, new FeatureTileLayerSource(feature, style));
    }
}
