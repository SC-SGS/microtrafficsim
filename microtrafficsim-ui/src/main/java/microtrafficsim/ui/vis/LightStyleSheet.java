package microtrafficsim.ui.vis;

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


class LightStyleSheet implements StyleSheet {

    private static final float SCALE_MAXLEVEL = (float) (1.0 / Math.pow(2, 19));

    private ParserConfig                       parserConfig;
    private Color                              colorBackground;
    private ArrayList<MapFeatureDefinition<?>> features;
    private ArrayList<TileLayerDefinition>     layers;

    {
        // parser configuration
        parserConfig = new ParserConfig(256, 512);

        // color definitions
        colorBackground = Color.fromRGB(0xFFFFFF);

        Color inlineColor = Color.fromRGB(0xFDFDFD);

        Color[] colors = {
                Color.fromRGB(0xFF6F69),      // motorway
                Color.fromRGB(0x659118),      // trunk
                Color.fromRGB(0xDB9E36),      // primary
                Color.fromRGB(0x105B63),      // secondary
                Color.from(177, 98, 134),     // tertiary
                Color.from(104, 157, 106),    // unclassified
                Color.from(124, 111, 100),    // residential
                Color.from(146, 131, 116),    // road
                Color.from(146, 131, 116),    // living-street
        };


        // feature predicates
        Predicate<Way> prMotorway     = new MajorStreetBasePredicate("motorway");
        Predicate<Way> prTrunk        = new MajorStreetBasePredicate("trunk");
        Predicate<Way> prPrimary      = new MajorStreetBasePredicate("primary");
        Predicate<Way> prSecondary    = new MajorStreetBasePredicate("secondary");
        Predicate<Way> prTertiary     = new MajorStreetBasePredicate("tertiary");
        Predicate<Way> prUnclassified = new MinorStreetBasePredicate("unclassified");
        Predicate<Way> prResidential  = new MinorStreetBasePredicate("residential");
        Predicate<Way> prRoad         = new MinorStreetBasePredicate("road");
        Predicate<Way> prLivingStreet = new MinorStreetBasePredicate("living_street");

        // define and add the features
        MapFeatureGenerator<Street> generator = new StreetFeatureGenerator();

        features = new ArrayList<>();
        features.add(genStreetFeatureDef("streets:motorway",      generator, prMotorway));
        features.add(genStreetFeatureDef("streets:trunk",         generator, prTrunk));
        features.add(genStreetFeatureDef("streets:primary",       generator, prPrimary));
        features.add(genStreetFeatureDef("streets:secondary",     generator, prSecondary));
        features.add(genStreetFeatureDef("streets:tertiary",      generator, prTertiary));
        features.add(genStreetFeatureDef("streets:unclassified",  generator, prUnclassified));
        features.add(genStreetFeatureDef("streets:residential",   generator, prResidential));
        features.add(genStreetFeatureDef("streets:road",          generator, prRoad));
        features.add(genStreetFeatureDef("streets:living_street", generator, prLivingStreet));

        // styles
        ShaderProgramSource streets = getStreetShader();

        Style sMotorwayOutline       = genStyle(streets, colors[0], 60.f, SCALE_MAXLEVEL);
        Style sTrunkOutline          = genStyle(streets, colors[1], 60.f, SCALE_MAXLEVEL);
        Style sPrimaryOutline        = genStyle(streets, colors[2], 50.f, SCALE_MAXLEVEL);
        Style sSecondaryOutline      = genStyle(streets, colors[3], 50.f, SCALE_MAXLEVEL);
        Style sTertiaryOutline       = genStyle(streets, colors[4], 50.f, SCALE_MAXLEVEL);
        Style sUnclassifiedOutline   = genStyle(streets, colors[5], 40.f, SCALE_MAXLEVEL);
        Style sResidentialOutline    = genStyle(streets, colors[6], 40.f, SCALE_MAXLEVEL);
        Style sRoadOutline           = genStyle(streets, colors[7], 40.f, SCALE_MAXLEVEL);
        Style sLivingStreetOutline   = genStyle(streets, colors[8], 32.f, SCALE_MAXLEVEL);

        Style sMotorwayInline        = genStyle(streets, inlineColor, 48.f, SCALE_MAXLEVEL);
        Style sTrunkInline           = genStyle(streets, inlineColor, 48.f, SCALE_MAXLEVEL);
        Style sPrimaryInline         = genStyle(streets, inlineColor, 40.f, SCALE_MAXLEVEL);
        Style sSecondaryInline       = genStyle(streets, inlineColor, 40.f, SCALE_MAXLEVEL);
        Style sTertiaryInline        = genStyle(streets, inlineColor, 40.f, SCALE_MAXLEVEL);
        Style sUnclassifiedInline    = genStyle(streets, inlineColor, 30.f, SCALE_MAXLEVEL);
        Style sResidentialInline     = genStyle(streets, inlineColor, 30.f, SCALE_MAXLEVEL);
        Style sRoadInline            = genStyle(streets, inlineColor, 30.f, SCALE_MAXLEVEL);
        Style sLivingStreetInline    = genStyle(streets, inlineColor, 24.f, SCALE_MAXLEVEL);

        Style sMotorwayOutlineL      = genStyle(streets, colors[0], 80.f, SCALE_MAXLEVEL);
        Style sTrunkOutlineL         = genStyle(streets, colors[1], 80.f, SCALE_MAXLEVEL);
        Style sPrimaryOutlineL       = genStyle(streets, colors[2], 70.f, SCALE_MAXLEVEL);
        Style sSecondaryOutlineL     = genStyle(streets, colors[3], 70.f, SCALE_MAXLEVEL);
        Style sTertiaryOutlineL      = genStyle(streets, colors[4], 70.f, SCALE_MAXLEVEL);
        Style sUnclassifiedOutlineL  = genStyle(streets, colors[5], 60.f, SCALE_MAXLEVEL);
        Style sResidentialOutlineL   = genStyle(streets, colors[6], 60.f, SCALE_MAXLEVEL);
        Style sRoadOutlineL          = genStyle(streets, colors[7], 60.f, SCALE_MAXLEVEL);
        Style sLivingStreetOutlineL  = genStyle(streets, colors[8], 45.f, SCALE_MAXLEVEL);

        Style sMotorwayOutlineXL     = genStyle(streets, colors[0], 95.f, SCALE_MAXLEVEL);
        Style sTrunkOutlineXL        = genStyle(streets, colors[1], 95.f, SCALE_MAXLEVEL);
        Style sPrimaryOutlineXL      = genStyle(streets, colors[2], 85.f, SCALE_MAXLEVEL);
        Style sSecondaryOutlineXL    = genStyle(streets, colors[3], 85.f, SCALE_MAXLEVEL);
        Style sTertiaryOutlineXL     = genStyle(streets, colors[4], 85.f, SCALE_MAXLEVEL);
        Style sUnclassifiedOutlineXL = genStyle(streets, colors[5], 75.f, SCALE_MAXLEVEL);
        Style sResidentialOutlineXL  = genStyle(streets, colors[6], 75.f, SCALE_MAXLEVEL);
        Style sRoadOutlineXL         = genStyle(streets, colors[7], 75.f, SCALE_MAXLEVEL);
        Style sLivingStreetOutlineXL = genStyle(streets, colors[8], 60.f, SCALE_MAXLEVEL);

        // layers
        int index = 0;
        layers    = new ArrayList<>();
        layers.add(genLayer("streets:living_street:outline",    index++, 16, 19, "streets:living_street", sLivingStreetOutline));
        layers.add(genLayer("streets:road:outline",             index++, 17, 19, "streets:road",          sRoadOutline));
        layers.add(genLayer("streets:residential:outline",      index++, 16, 19, "streets:residential",   sResidentialOutline));
        layers.add(genLayer("streets:unclassified:outline",     index++, 16, 19, "streets:unclassified",  sUnclassifiedOutline));
        layers.add(genLayer("streets:tertiary:outline",         index++, 16, 19, "streets:tertiary",      sTertiaryOutline));
        layers.add(genLayer("streets:secondary:outline",        index++, 16, 19, "streets:secondary",     sSecondaryOutline));
        layers.add(genLayer("streets:primary:outline",          index++, 16, 19, "streets:primary",       sPrimaryOutline));
        layers.add(genLayer("streets:trunk:outline",            index++, 16, 19, "streets:trunk",         sTrunkOutline));
        layers.add(genLayer("streets:motorway:outline",         index++, 16, 19, "streets:motorway",      sMotorwayOutline));

        layers.add(genLayer("streets:living_street:inline",     index++, 16, 19, "streets:living_street", sLivingStreetInline));
        layers.add(genLayer("streets:road:inline",              index++, 17, 19, "streets:road",          sRoadInline));
        layers.add(genLayer("streets:residential:inline",       index++, 16, 19, "streets:residential",   sResidentialInline));
        layers.add(genLayer("streets:unclassified:inline",      index++, 16, 19, "streets:unclassified",  sUnclassifiedInline));
        layers.add(genLayer("streets:tertiary:inline",          index++, 16, 19, "streets:tertiary",      sTertiaryInline));
        layers.add(genLayer("streets:secondary:inline",         index++, 16, 19, "streets:secondary",     sSecondaryInline));
        layers.add(genLayer("streets:primary:inline",           index++, 16, 19, "streets:primary",       sPrimaryInline));
        layers.add(genLayer("streets:trunk:inline",             index++, 16, 19, "streets:trunk",         sTrunkInline));
        layers.add(genLayer("streets:motorway:inline",          index++, 16, 19, "streets:motorway",      sMotorwayInline));

        layers.add(genLayer("streets:living_street:outline:l",  index++, 14, 15, "streets:living_street", sLivingStreetOutlineL));
        layers.add(genLayer("streets:road:outline:l",           index++, 14, 16, "streets:road",          sRoadOutlineL));
        layers.add(genLayer("streets:residential:outline:l",    index++, 14, 15, "streets:residential",   sResidentialOutlineL));
        layers.add(genLayer("streets:unclassified:outline:l",   index++, 14, 15, "streets:unclassified",  sUnclassifiedOutlineL));
        layers.add(genLayer("streets:tertiary:outline:l",       index++, 14, 15, "streets:tertiary",      sTertiaryOutlineL));
        layers.add(genLayer("streets:secondary:outline:l",      index++, 14, 15, "streets:secondary",     sSecondaryOutlineL));
        layers.add(genLayer("streets:primary:outline:l",        index++, 14, 15, "streets:primary",       sPrimaryOutlineL));
        layers.add(genLayer("streets:trunk:outline:l",          index++, 14, 15, "streets:trunk",         sTrunkOutlineL));
        layers.add(genLayer("streets:motorway:outline:l",       index++, 14, 15, "streets:motorway",      sMotorwayOutlineL));

        layers.add(genLayer("streets:living_street:outline:xl", index++, 13, 13, "streets:living_street", sLivingStreetOutlineXL));
        layers.add(genLayer("streets:road:outline:xl",          index++, 13, 13, "streets:road",          sRoadOutlineXL));
        layers.add(genLayer("streets:residential:outline:xl",   index++, 12, 13, "streets:residential",   sResidentialOutlineXL));
        layers.add(genLayer("streets:unclassified:outline:xl",  index++, 12, 13, "streets:unclassified",  sUnclassifiedOutlineXL));
        layers.add(genLayer("streets:tertiary:outline:xl",      index++,  0, 13, "streets:tertiary",      sTertiaryOutlineXL));
        layers.add(genLayer("streets:secondary:outline:xl",     index++,  0, 13, "streets:secondary",     sSecondaryOutlineXL));
        layers.add(genLayer("streets:primary:outline:xl",       index++,  0, 13, "streets:primary",       sPrimaryOutlineXL));
        layers.add(genLayer("streets:trunk:outline:xl",         index++,  0, 13, "streets:trunk",         sTrunkOutlineXL));
        layers.add(genLayer("streets:motorway:outline:xl",      index++,  0, 13, "streets:motorway",      sMotorwayOutlineXL));
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
        return new MapFeatureDefinition<>(name, parserConfig.generatorIndexOfStreetGraph + 1, generator,
                                          n -> false, predicate);
    }

    private ShaderProgramSource getStreetShader() {
        Resource vert = new PackagedResource(LightStyleSheet.class, "/shaders/features/streets/streets.vs");
        Resource frag = new PackagedResource(LightStyleSheet.class, "/shaders/features/streets/streets.fs");
        Resource geom = new PackagedResource(LightStyleSheet.class, "/shaders/features/streets/streets_round.gs");

        ShaderProgramSource prog = new ShaderProgramSource("streets");
        prog.addSource(GL3.GL_VERTEX_SHADER, vert);
        prog.addSource(GL3.GL_FRAGMENT_SHADER, frag);
        prog.addSource(GL3.GL_GEOMETRY_SHADER, geom);

        return prog;
    }

    private Style genStyle(ShaderProgramSource shader, Color color, float linewidth, float scalenorm) {
        Style style = new Style(shader);
        style.setUniformSupplier("u_color", color::toVec4f);
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
            return w.visible && type.equals(w.tags.get("highway"))
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
            return w.visible && (type.equals(w.tags.get("highway")) || link.equals(w.tags.get("highway")))
                    && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
        }
    }
}
