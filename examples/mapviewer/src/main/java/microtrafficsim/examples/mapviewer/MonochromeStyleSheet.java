package microtrafficsim.examples.mapviewer;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.parser.features.MapFeatureDefinition;
import microtrafficsim.core.parser.features.MapFeatureGenerator;
import microtrafficsim.core.parser.features.polygons.PolygonFeatureGenerator;
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
import java.util.function.IntPredicate;
import java.util.function.Predicate;


/**
 * A light style-sheet for the MapViewer.
 *
 * @author Maximilian Luz
 */
class MonochromeStyleSheet implements MapStyleSheet {

    private static final float SCALE_MAXLEVEL = (float) (1.0 / Math.pow(2, 19));

    private Color                              colorBackground;
    private ArrayList<MapFeatureDefinition<?>> features;
    private ArrayList<LayerDefinition>         layers;

    {
        /* base feature names */
        String[] names = {
                "streets:motorway",
                "streets:trunk",
                "streets:primary",
                "streets:secondary",
                "streets:tertiary",
                "streets:unclassified",
                "streets:residential",
                "streets:road",
                "streets:living_street",
        };

        /* color definitions */
        colorBackground = Color.fromRGB(0x131313);

        Color[] colorsOutline = {
                Color.fromRGB(0x000000),      // motorway
                Color.fromRGB(0x000000),      // trunk
                Color.fromRGB(0x000000),      // primary
                Color.fromRGB(0x000000),      // secondary
                Color.fromRGB(0x000000),      // tertiary
                Color.fromRGB(0x000000),      // unclassified
                Color.fromRGB(0x000000),      // residential
                Color.fromRGB(0x000000),      // road
                Color.fromRGB(0x000000),      // living_street
        };

        Color[] colorsInline = {
                Color.fromRGB(0xA0A0A0),      // motorway
                Color.fromRGB(0x969696),      // trunk
                Color.fromRGB(0x8C8C8C),      // primary
                Color.fromRGB(0x828282),      // secondary
                Color.fromRGB(0x787878),      // tertiary
                Color.fromRGB(0x6E6E6E),      // unclassified
                Color.fromRGB(0x646464),      // residential
                Color.fromRGB(0x5A5A5A),      // road
                Color.fromRGB(0x505050),      // living_street
        };

        /* line width */
        LineWidthBaseFunction lineWidthBase = (offset, base, exp1, exp2, zoom) -> {
            if (zoom >= 12)
                return offset + base * (float) Math.pow(exp1, (19 - zoom));
            else if (zoom >= 10)
                return offset + base * (float) Math.pow(exp1, (19 - 12)) + base * (float) Math.pow(exp2, 12 - zoom);
            else
                return offset + base * (float) Math.pow(exp1, (19 - 12)) - base * (float) Math.pow(exp2, 12 - 11);
        };

        LineWidthFunction[] linewidthOutline = {
                zoom -> lineWidthBase.get(50.f, 20.f, 1.30f, 0.75f, zoom),
                zoom -> lineWidthBase.get(50.f, 20.f, 1.30f, 0.75f, zoom),
                zoom -> lineWidthBase.get(40.f, 20.f, 1.20f, 0.75f, zoom),
                zoom -> lineWidthBase.get(40.f, 20.f, 1.20f, 0.75f, zoom),
                zoom -> lineWidthBase.get(40.f, 20.f, 1.20f, 0.75f, zoom),
                zoom -> lineWidthBase.get(30.f, 20.f, 1.15f, 0.75f, zoom),
                zoom -> lineWidthBase.get(30.f, 20.f, 1.15f, 0.75f, zoom),
                zoom -> lineWidthBase.get(30.f, 20.f, 1.15f, 0.75f, zoom),
                zoom -> lineWidthBase.get(25.f, 20.f, 1.10f, 0.75f, zoom),
        };

        LineWidthFunction[] linewidthInline = {
                zoom -> lineWidthBase.get(45.f, 20.f, 1.32f, 0.3f, zoom),
                zoom -> lineWidthBase.get(45.f, 20.f, 1.32f, 0.3f, zoom),
                zoom -> lineWidthBase.get(35.f, 20.f, 1.22f, 0.3f, zoom),
                zoom -> lineWidthBase.get(35.f, 20.f, 1.22f, 0.3f, zoom),
                zoom -> lineWidthBase.get(35.f, 20.f, 1.22f, 0.3f, zoom),
                zoom -> lineWidthBase.get(25.f, 20.f, 1.17f, 0.3f, zoom),
                zoom -> lineWidthBase.get(25.f, 20.f, 1.17f, 0.3f, zoom),
                zoom -> lineWidthBase.get(25.f, 20.f, 1.17f, 0.3f, zoom),
                zoom -> lineWidthBase.get(20.f, 20.f, 1.12f, 0.3f, zoom),
        };

        IntPredicate[] activeOutline = {
                zoom -> true,
                zoom -> zoom >= 5,
                zoom -> zoom >= 7,
                zoom -> zoom >= 9,
                zoom -> zoom >= 9,
                zoom -> zoom >= 12,
                zoom -> zoom >= 12,
                zoom -> zoom >= 12,
                zoom -> zoom >= 13
        };

        IntPredicate[] activeInline = {
                zoom -> true,
                zoom -> zoom >= 5,
                zoom -> zoom >= 7,
                zoom -> zoom >= 9,
                zoom -> zoom >= 9,
                zoom -> zoom >= 12,
                zoom -> zoom >= 12,
                zoom -> zoom >= 12,
                zoom -> zoom >= 13
        };

        /* feature predicates */
        @SuppressWarnings("unchecked")
        Predicate<Way>[] predicates = new Predicate[]{
                new MajorStreetBasePredicate("motorway"),
                new MajorStreetBasePredicate("trunk"),
                new MajorStreetBasePredicate("primary"),
                new MajorStreetBasePredicate("secondary"),
                new MajorStreetBasePredicate("tertiary"),
                new MinorStreetBasePredicate("unclassified"),
                new MinorStreetBasePredicate("residential"),
                new MinorStreetBasePredicate("road"),
                new MinorStreetBasePredicate("living_street")
        };

        features = new ArrayList<>(names.length + 2);
        layers   = new ArrayList<>(2 * names.length + 2);

        /* define and add the features */
        MapFeatureGenerator<Street>  streetgen = new StreetFeatureGenerator();
        MapFeatureGenerator<Polygon> polygen   = new PolygonFeatureGenerator();

        features.add(genPolygonFeatureDef("water", polygen,
                way -> "water".equals(way.tags.get("natural")) || "riverbank".equals(way.tags.get("waterway")))
        );

        features.add(genPolygonFeatureDef("landuse",   polygen, way -> way.tags.get("landuse") != null));
        features.add(genPolygonFeatureDef("buildings", polygen, way -> way.tags.get("building") != null));

        for (int i = 0; i < names.length; i++)
            features.add(genStreetFeatureDef(names[i], streetgen, predicates[i]));

        /* styles and layers*/
        ShaderProgramSource streets = getStreetShader();
        ShaderProgramSource polygons = getPolygonShader();

        int index = 0;

        layers.add(genLayer("ply:water",     index++,  0, 19, "water",     genPolygonStyle(polygons, Color.fromRGBA(0x2D4747A0))));
        layers.add(genLayer("ply:landuse",   index++, 12, 19, "landuse",   genPolygonStyle(polygons, Color.fromRGB(0x1D1D1D))));
        layers.add(genLayer("ply:buildings", index++, 12, 19, "buildings", genPolygonStyle(polygons, Color.fromRGB(0x382629))));

        for (int zoom = 19; zoom >= 0; zoom--) {
            for (int i = names.length - 1; i >= 0; i--) {
                if (activeOutline[i].test(zoom)) {
                    Style style = genStreetStyle(streets, colorsOutline[i], linewidthOutline[i].get(zoom), SCALE_MAXLEVEL);
                    layers.add(genLayer(names[i] + ":outline:" + zoom, index++, zoom, zoom, names[i], style));
                }
            }

            for (int i = names.length - 1; i >= 0; i--) {
                if (activeInline[i].test(zoom)) {
                    Style style = genStreetStyle(streets, colorsInline[i], linewidthInline[i].get(zoom), SCALE_MAXLEVEL);
                    layers.add(genLayer(names[i] + ":inline:" + zoom, index++, zoom, zoom, names[i], style));
                }
            }
        }
    }


    @Override
    public Color getBackgroundColor() {
        return colorBackground;
    }

    @Override
    public Color getTileBackgroundColor() {
        return colorBackground;
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

    private MapFeatureDefinition<Polygon> genPolygonFeatureDef(String name, MapFeatureGenerator<Polygon> generator,
                                                               Predicate<Way> predicate) {
        FeatureDependency dependency = new FeatureDependency();
        dependency.addBefore(DEPENDS_ON_WAY_CLIPPING);
        dependency.addBefore(DEPENDS_ON_UNIFICATION);
        dependency.addBefore(DEPENDS_ON_STREETGRAPH);

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
     * Return the source of the shader used for polygon-rendering.
     *
     * @return the created shader-sources.
     */
    private ShaderProgramSource getPolygonShader() {
        Resource vert = new PackagedResource(MonochromeStyleSheet.class, "/shaders/basic.vs");
        Resource frag = new PackagedResource(MonochromeStyleSheet.class, "/shaders/basic.fs");

        ShaderProgramSource prog = new ShaderProgramSource("/shaders/basic");
        prog.addSource(GL3.GL_VERTEX_SHADER, vert);
        prog.addSource(GL3.GL_FRAGMENT_SHADER, frag);

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
    private Style genStreetStyle(ShaderProgramSource shader, Color color, float linewidth, float scalenorm) {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
        style.setUniformSupplier("u_linewidth", () -> linewidth);
        style.setUniformSupplier("u_viewscale_norm", () -> scalenorm);
        style.setProperty("adjacency_primitives", true);
        style.setProperty("use_joins_when_possible", true);
        return style;
    }

    /**
     * Generate a style for polygons based on the specific properties.
     *
     * @param shader    the shader to be used in the generated style.
     * @param color     the color to be used in generated style.
     * @return the generated style.
     */
    private Style genPolygonStyle(ShaderProgramSource shader, Color color) {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
        return style;
    }

    /**
     * Generate a LayerDefinition for features, based on the specific properties.
     *
     * @param name    the name of the generated grid.
     * @param index   the index of the  generated grid.
     * @param min     the minimum zoom-level at which the generated grid is visible.
     * @param max     the maximum zoom-level at which the generated grid is visible.
     * @param feature the feature-name of the feature on which the grid is based on.
     * @param style   the style to be used for rendering.
     * @return the generated LayerDefinition.
     */
    private LayerDefinition genLayer(String name, int index, int min, int max, String feature, Style style) {
        return new LayerDefinition(name, index, min, max, new FeatureTileLayerSource(feature, style));
    }


    private interface LineWidthFunction {
        float get(int zoom);
    }

    private interface LineWidthBaseFunction {
        float get(float offset, float base, float exp1, float exp2, int zoom);
    }


    /**
     * A predicate usable to select minor streets.
     */
    private static class MinorStreetBasePredicate implements Predicate<Way> {

        private final String type;

        /**
         * Create a new predicate based on the given type-name.
         * @param type the name of the street-type to select.
         */
        MinorStreetBasePredicate(String type) {
            this.type = type;
        }

        @Override
        public boolean test(Way w) {
            return w.visible && type.equals(w.tags.get("highway"))
                    && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
        }
    }

    /**
     * A predicate usable to select major streets (i.e. streets and their associated link-type).
     */
    private static class MajorStreetBasePredicate implements Predicate<Way> {
        private final String type;
        private final String link;

        /**
         * Create a new predicate based on the given type-name.
         * @param type the name of the street-type to select.
         */
        MajorStreetBasePredicate(String type) {
            this.type = type;
            this.link = type + "_link";
        }

        @Override
        public boolean test(Way w) {
            return w.visible && (type.equals(w.tags.get("highway")) || link.equals(w.tags.get("highway")))
                    && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
        }
    }
}
