package microtrafficsim.examples.mapviewer;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.map.style.predicates.StreetBasePredicate;
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
 * A light style-sheet for the MapViewer, includes Polygons.
 *
 * @author Maximilian Luz
 */
class MonochromeStyleSheet extends microtrafficsim.core.map.style.impl.MonochromeStyleSheet {


    @Override
    protected void initialize() {
        MapFeatureGenerator<Polygon> generator = new PolygonFeatureGenerator();

        features.add(genPolygonFeatureDef("water", generator,
                way -> "water".equals(way.tags.get("natural")) || "riverbank".equals(way.tags.get("waterway")))
        );

        features.add(genPolygonFeatureDef("landuse",   generator, way -> way.tags.get("landuse") != null));
        features.add(genPolygonFeatureDef("buildings", generator, way -> way.tags.get("building") != null));

        ShaderProgramSource shader = getPolygonShader();

        layers.add(genLayer("ply:water",     layers.size(),  0, 19, "water",     genPolygonStyle(shader, Color.fromRGBA(0x2D4747A0))));
        layers.add(genLayer("ply:landuse",   layers.size(), 12, 19, "landuse",   genPolygonStyle(shader, Color.fromRGB(0x1D1D1D))));
        layers.add(genLayer("ply:buildings", layers.size(), 12, 19, "buildings", genPolygonStyle(shader, Color.fromRGB(0x382629))));

        super.initialize();
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
}
