package microtrafficsim.core.map.style;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;
import microtrafficsim.core.map.style.predicates.MajorStreetBasePredicate;
import microtrafficsim.core.map.style.predicates.MinorStreetBasePredicate;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.parser.features.MapFeatureGenerator;
import microtrafficsim.core.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * <p>
 * A basic style-sheet for the MapViewer. It implements basic functionality, so the sub-classes does only have to
 * care for definitions like colors.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicStyleSheet implements StyleSheet {

    private static final float SCALE_MAXLEVEL = (float) (1.0 / Math.pow(2, 19));

    private ArrayList<MapFeatureDefinition<?>> features;
    private ArrayList<LayerDefinition>         layers;

    /**
     * This constructor calls {@link #initStyleCreation(String[])} before it constructs this style sheet. This method
     * can be used e.g. to define colors in sub-classes.
     */
    protected BasicStyleSheet() {

        /* base feature names */
        String[] simpleNames = new String[]{
                "motorway",
                "trunk",
                "primary",
                "secondary",
                "tertiary",
                "unclassified",
                "residential",
                "road",
                "living_street"
        };

        /* change names for feature definition */
        String[] fullNames = new String[simpleNames.length];
        for (int i = 0; i < simpleNames.length; i++)
            fullNames[i] = "streets:" + simpleNames[i];

        /*
         * This method call is needed here to make the style-sheet-definition as easy as possible for sub-classes.
         * The other option would be a method like "create()", which has to be called in the sub-classes'
         * constructors AFTER all colors are defined.
         */
        initStyleCreation(simpleNames);

        /* feature predicates */
        @SuppressWarnings("unchecked")
        Predicate<Way>[] predicates = new Predicate[simpleNames.length];
        for (int i = 0; i < predicates.length; i++) {
            if (i <= 4)
                predicates[i] = new MajorStreetBasePredicate(simpleNames[i]);
            else
                predicates[i] = new MinorStreetBasePredicate(simpleNames[i]);
        }

        /* define and add the features */
        MapFeatureGenerator<Street> generator = new StreetFeatureGenerator();

        features = new ArrayList<>(fullNames.length);
        for (int i = 0; i < fullNames.length; i++)
            features.add(genStreetFeatureDef(fullNames[i], generator, predicates[i]));

        /* styles and layers */
        ShaderProgramSource streets = getStreetShader();

        int index = 0;
        layers = new ArrayList<>();

        for (int zoom = 19; zoom >= 0; zoom--) {
            for (int i = 0; i < simpleNames.length; i++) {
                String simpleName = simpleNames[i];
                String fullName = fullNames[i];

                if (isOutlineActive(simpleName, zoom)) {
                    Style style = genStyle(
                            streets,
                            getColorOutline(simpleName),
                            getLineWidthOutline(simpleName, zoom),
                            SCALE_MAXLEVEL);
                    layers.add(genLayer(fullName + ":outline:" + zoom,
                            index++, zoom, zoom, fullName, style));
                }
            }

            for (int i = 0; i < simpleNames.length; i++) {
                String simpleName = simpleNames[i];
                String fullName = fullNames[i];

                if (isInlineActive(simpleName, zoom)) {
                    Style style = genStyle(
                            streets,
                            getColorInline(simpleName),
                            getLineWidthInline(simpleName, zoom),
                            SCALE_MAXLEVEL);
                    layers.add(genLayer(fullName + ":inline:" + zoom,
                            index++, zoom, zoom, fullName, style));
                }
            }
        }
    }

    /*
    |============================|
    | definition for sub-classes |
    |============================|
    */
    /**
     * This method is called in {@link BasicStyleSheet#BasicStyleSheet()}. It's empty per default.
     *
     * @param streetFeatureNames the street features name, so sub-classes can define colors depending on the street
     *                           feature.
     */
    protected void initStyleCreation(String[] streetFeatureNames) {

    }

    protected boolean isOutlineActive(String streetFeatureName, int zoom) {

        switch (streetFeatureName) {
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
            case "living-street":
                return zoom >= 13;
            default:
                return false;
        }
    }

    protected boolean isInlineActive(String streetFeatureName, int zoom) {
        return isOutlineActive(streetFeatureName, zoom);
    }

    protected abstract Color getColorOutline(String streetFeatureName);

    protected abstract Color getColorInline(String streetFeatureName);

    protected abstract float getLineWidthOutline(String streetFeatureName, int zoom);

    protected abstract float getLineWidthInline(String streetFeatureName, int zoom);

    /*
    |================|
    | (i) StyleSheet |
    |================|
    */
    @Override
    public Collection<MapFeatureDefinition<?>> getFeatureDefinitions() {
        return features;
    }

    @Override
    public Collection<LayerDefinition> getLayers() {
        return layers;
    }

    /*
    |============|
    | generation |
    |============|
    */
    /**
     * Generates a basic street-feature definition.
     *
     * @param name      the name of the feature.
     * @param generator the generator for the feature.
     * @param predicate the predicate to select the Ways contained in this feature.
     * @return the created MapFeatureDefinition.
     */
    private MapFeatureDefinition<Street> genStreetFeatureDef(String name, MapFeatureGenerator<Street> generator,
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
    private ShaderProgramSource getStreetShader() {
        Resource vert = new PackagedResource(MonochromeStyleSheet.class, "/shaders/features/streets/streets.vs");
        Resource frag = new PackagedResource(MonochromeStyleSheet.class, "/shaders/features/streets/streets.fs");
        Resource geom = new PackagedResource(MonochromeStyleSheet.class, "/shaders/features/streets/streets_round.gs");

        ShaderProgramSource prog = new ShaderProgramSource("streets");
        prog.addSource(GL3.GL_VERTEX_SHADER, vert);
        prog.addSource(GL3.GL_FRAGMENT_SHADER, frag);
        prog.addSource(GL3.GL_GEOMETRY_SHADER, geom);

        return prog;
    }

    /**
     * Generate a style for streets based on the specific properties.
     *
     * @param shader    the shader to be used in the generated style.
     * @param color     the color to be used in generated style.
     * @param linewidth the line-width of the generated style.
     * @param scalenorm the scale-normal of the generated style.
     * @return the generated style.
     */
    private Style genStyle(ShaderProgramSource shader, Color color, float linewidth, float scalenorm) {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
        style.setUniformSupplier("u_linewidth", () -> linewidth);
        style.setUniformSupplier("u_viewscale_norm", () -> scalenorm);
        style.setProperty("adjacency_primitives", true);
        style.setProperty("use_joins_when_possible", true);
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
    private LayerDefinition genLayer(String name, int index, int min, int max, String feature, Style style) {
        return new LayerDefinition(name, index, min, max, new FeatureTileLayerSource(feature, style));
    }
}
