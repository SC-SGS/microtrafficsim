package microtrafficsim.core.convenience;

import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;

import java.util.function.Predicate;


public class DefaultParserConfig {
    public static OSMParser.Config get(SimulationConfig config) {
        return get(config.visualization.style, config);
    }

    public static OSMParser.Config get(MapStyleSheet style) {
        return get(style, null);
    }

    public static OSMParser.Config get(MapStyleSheet style, SimulationConfig config) {
        /* global properties for (all) generators */
        FeatureGenerator.Properties genprops = new FeatureGenerator.Properties();
        genprops.bounds = FeatureGenerator.Properties.BoundaryManagement.CLIP;

        /* create a configuration, add factories for parsed components */
        OSMParser.Config parser = new OSMParser.Config().setGeneratorProperties(genprops);

        StreetGraphFeatureDefinition streetgraph = null;
        if (config != null) {
            // predicates to match/select features
            Predicate<Way> streetgraphMatcher = w -> {
                if (!w.visible) return false;
                if (w.tags.get("highway") == null) return false;
                if (w.tags.get("area") != null && !w.tags.get("area").equals("no")) return false;

                switch (w.tags.get("highway")) {
                    case "motorway":      return true;
                    case "trunk":         return true;
                    case "primary":       return true;
                    case "secondary":     return true;
                    case "tertiary":      return true;
                    case "unclassified":  return true;
                    case "residential":   return true;
                    // case "service":       return true;

                    case "motorway_link": return true;
                    case "trunk_link":    return true;
                    case "primary_link":  return true;
                    case "tertiary_link": return true;

                    case "living_street": return true;
                    // case "track":         return true;
                    case "road":          return true;
                }

                return false;
            };

            streetgraph = new StreetGraphFeatureDefinition(
                    "streetgraph",
                    new FeatureDependency(OSMParser.PLACEHOLDER_UNIFICATION, null),
                    new StreetGraphGenerator(config),
                    n -> false,
                    streetgraphMatcher
            );

            parser.setStreetGraphFeatureDefinition(streetgraph);
        }

        /* replace the style-placeholders with the feature-definitions/placeholders used by the osm-processor */
        style.replaceDependencyPlaceholders(OSMParser.PLACEHOLDER_WAY_CLIPPING, OSMParser.PLACEHOLDER_UNIFICATION,
                streetgraph);

        parser.putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory());

        /* add the features defined in the style to the parser */
        style.getFeatureDefinitions().forEach(parser::putMapFeatureDefinition);

        return parser;
    }
}
