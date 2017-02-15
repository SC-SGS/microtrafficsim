package microtrafficsim.examples.parser;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.parser.*;
import microtrafficsim.core.parser.features.*;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import java.io.File;
import java.util.function.Predicate;


/**
 * An example showing the usage of the OpenStreetMap parser-framework.
 * The map to be parsed can be specified via the command-line options.
 *
 * @author Maximilian Luz
 */
public class ParserExample {
    private static final String DEFAULT_OSM_XML = "map.processing";


    public static void main(String[] args) throws Exception {
        File file;

        if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                printUsage();
                return;

            default:
                file = new File(args[0]);
            }
        } else {
            file = new File(DEFAULT_OSM_XML);
        }


        // create parser and parse
        OSMParser.Result result = createParser().parse(file);

        // do stuff with parsed data
        MapSegment  segment     = result.segment;
        Graph       streetgraph = result.streetgraph;

        System.out.println(segment.getBounds().toString());
        System.out.println(segment.require("streets").getData().length);
    }

    private static void printUsage() {
        System.out.println("MicroTrafficSim - OSM Parser Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  parser                Run this example with the default map-file (map.processing)");
        System.out.println("  parser <file>         Run this example with the specified map-file");
        System.out.println("  parser --help | -h    Show this help message.");
        System.out.println("");
    }


    /**
     * Create a basic OSMParser which creates a {@code StreetGraph} and an
     * associated {@code Feature<Street>}.
     *
     * @return the created OSMParser.
     */
    private static OSMParser createParser() {

        // predicates to match/select features
        Predicate<Node> streetgraphNodeMatcher = (Node n) -> false;

        Predicate<Way> streetgraphWayMatcher = (Way w) -> {
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
            case "service":       return true;

            case "motorway_link": return true;
            case "trunk_link":    return true;
            case "primary_link":  return true;
            case "tertiary_link": return true;

            case "living_street": return true;
            case "track":         return true;
            case "road":          return true;
            }

            return false;
        };

        // create the feature generators
        MapFeatureGenerator<Street> streetsGenerator = new StreetFeatureGenerator();
        StreetGraphGenerator        sgGenerator      = new StreetGraphGenerator(new ScenarioConfig());

        // define the features
        StreetGraphFeatureDefinition sg = new StreetGraphFeatureDefinition(
                "streetgraph",
                new FeatureDependency(),            // street-graph are managed automatically
                sgGenerator,
                streetgraphNodeMatcher,
                streetgraphWayMatcher
        );

        FeatureDependency streetsDependency = new FeatureDependency();
        streetsDependency.addRequires(sg);                                  // streets-feature depends on street-graph
        streetsDependency.addRequires(OSMParser.PLACEHOLDER_UNIFICATION);   // streets-feature depends on street
                                                                            //  unification step

        MapFeatureDefinition<Street> streets = new MapFeatureDefinition<>(
                "streets",
                streetsDependency,
                streetsGenerator,
                streetgraphNodeMatcher,
                streetgraphWayMatcher
        );

        // set the generator properties to recalculate the map-bounds after parsing
        FeatureGenerator.Properties genprops = new FeatureGenerator.Properties();
        genprops.bounds = FeatureGenerator.Properties.BoundaryManagement.RECALCULATE;

        // generate the parser
        return new OSMParser.Config()
                .setGeneratorProperties(genprops)
                .setStreetGraphFeatureDefinition(sg)
                .putMapFeatureDefinition(streets)
                .putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory())
                .createParser();
    }
}
