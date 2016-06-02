package microtrafficsim.examples.mapviewer.tilebased;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.parser.MapFeatureDefinition;
import microtrafficsim.core.parser.MapFeatureGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.osm.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;


public class LightStyleSheet implements StyleSheet{

    private static final float SCALE_MAXLEVEL = (float) (1.0 / Math.pow(2, 19));

    private ParserConfig parserConfig;
    private Color colorBackground;
    private ArrayList<MapFeatureDefinition<?>> features;
    private ArrayList<TileLayerDefinition> layers;

    {
        // parser configuration
        parserConfig = new ParserConfig(256, 512);

        // color definitions
        colorBackground = Color.fromRGB(0xFFFFFF);

        // feature predicates
        Predicate<Way> prMotorway = new MajorStreetBasePredicate("motorway");
        Predicate<Way> prTrunk = new MajorStreetBasePredicate("trunk");
        Predicate<Way> prPrimary = new MajorStreetBasePredicate("primary");

        Predicate<Way> prOther = w -> {
            if (!w.visible) return false;
            if (w.tags.get("highway") == null) return false;
            if (w.tags.get("area") != null && !w.tags.get("area").equals("no")) return false;

            switch (w.tags.get("highway")) {
                case "secondary":		return true;
                case "tertiary":		return true;
                case "unclassified":	return true;
                case "residential":		return true;

                case "tertiary_link":	return true;
                case "secondary_link":	return true;

                case "living_street":	return true;
                case "track":			return true;
                case "road":			return true;
            }

            return false;
        };

        // define and add the features
        MapFeatureGenerator<Street> generator = new StreetFeatureGenerator();

        features = new ArrayList<>();
        features.add(genStreetFeatureDef("streets:motorway", generator, prMotorway));
        features.add(genStreetFeatureDef("streets:trunk", generator, prTrunk));
        features.add(genStreetFeatureDef("streets:primary", generator, prPrimary));
        features.add(genStreetFeatureDef("streets:other", generator, prOther));

        // styles
        ShaderProgramSource streets = getStreetShader();

        Style sMotorwayOutline = genStyle(streets, Color.fromRGB(0xFF7336), 46.f, SCALE_MAXLEVEL);
        Style sMotorwayInline = genStyle(streets, Color.fromRGB(0xFFFFFF), 40.f, SCALE_MAXLEVEL);
        Style sTrunkOutline = genStyle(streets, Color.fromRGB(0x8FC270), 46.f, SCALE_MAXLEVEL);
        Style sTrunkInline = genStyle(streets, Color.fromRGB(0xFFFFFF), 40.f, SCALE_MAXLEVEL);
        Style sPrimaryOutline = genStyle(streets, Color.fromRGB(0x0595D1), 46.f, SCALE_MAXLEVEL);
        Style sPrimaryInline = genStyle(streets, Color.fromRGB(0xFFFFFF), 40.f, SCALE_MAXLEVEL);
        Style sOtherOutline = genStyle(streets, Color.fromRGB(0x686868), 28.f, SCALE_MAXLEVEL);
        Style sOtherInline = genStyle(streets, Color.fromRGB(0xFFFFFF), 24.f, SCALE_MAXLEVEL);

        // layers
        int index = 0;
        layers = new ArrayList<>();
        layers.add(genLayer("streets:other:outline", index++, 0, 19, "streets:other", sOtherOutline));
        layers.add(genLayer("streets:primary:outline", index++, 0, 19, "streets:primary", sPrimaryOutline));
        layers.add(genLayer("streets:trunk:outline", index++, 0, 19, "streets:trunk", sTrunkOutline));
        layers.add(genLayer("streets:motorway:outline", index++, 0, 19, "streets:motorway", sMotorwayOutline));

        layers.add(genLayer("streets:other:inline", index++, 17, 19, "streets:other", sOtherInline));
        layers.add(genLayer("streets:primary:inline", index++, 17, 19, "streets:primary", sPrimaryInline));
        layers.add(genLayer("streets:trunk:inline", index++, 17, 19, "streets:trunk", sTrunkInline));
        layers.add(genLayer("streets:motorway:inline", index++, 17, 19, "streets:motorway", sMotorwayInline));
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
    public ParserConfig getParserConfiguration() {
        return parserConfig;
    }

    @Override
    public Collection<MapFeatureDefinition<?>> getFeatureDefinitions() {
        return features;
    }

    @Override
    public Collection<TileLayerDefinition> getLayers() {
        return layers;
    }


    private MapFeatureDefinition<Street> genStreetFeatureDef(String name, MapFeatureGenerator<Street> generator,
                                                             Predicate<Way> predicate) {
        return new MapFeatureDefinition<>(
                name,
                parserConfig.generatorIndexOfStreetGraph + 1,
                generator,
                n -> false,
                predicate
        );
    }

    private ShaderProgramSource getStreetShader() {
        Resource vert = new PackagedResource(Example.class, "/shaders/features/streets/streets.vs");
        Resource frag = new PackagedResource(Example.class, "/shaders/features/streets/streets.fs");
        Resource geom = new PackagedResource(Example.class, "/shaders/features/streets/streets_round.gs");

        ShaderProgramSource prog = new ShaderProgramSource("streets");
        prog.addSource(GL3.GL_VERTEX_SHADER, vert);
        prog.addSource(GL3.GL_FRAGMENT_SHADER, frag);
        prog.addSource(GL3.GL_GEOMETRY_SHADER, geom);

        return prog;
    }

    private Style genStyle(ShaderProgramSource shader, Color color, float linewidth, float scalenorm) {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", () -> color.toVec4f());
        style.setUniformSupplier("u_linewidth", () -> linewidth);
        style.setUniformSupplier("u_viewscale_norm", () -> scalenorm);
        style.setProperty("adjacency_primitives", true);
        style.setProperty("use_joins_when_possible", true);
        return style;
    }

    private TileLayerDefinition genLayer(String name, int index, int min, int max, String feature, Style style) {
        return new TileLayerDefinition(name, index, min, max, new FeatureTileLayerSource(feature, style));
    }


    private static class MinorStreetBasePredicate implements Predicate<Way> {

        private final String type;

        MinorStreetBasePredicate(String type) {
            this.type = type;
        }

        @Override
        public boolean test(Way w) {
            return w.visible
                    && type.equals(w.tags.get("highway"))
                    && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
        }
    }

    private static class MajorStreetBasePredicate implements Predicate<Way> {

        private final String type;
        private final String link;

        MajorStreetBasePredicate(String type) {
            this.type = type;
            this.link = type + "_link";
        }

        @Override
        public boolean test(Way w) {
            return w.visible
                    && (type.equals(w.tags.get("highway")) || link.equals(w.tags.get("highway")))
                    && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
        }
    }
}
