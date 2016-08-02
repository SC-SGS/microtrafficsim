package microtrafficsim.examples.parser;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.parser.*;
import microtrafficsim.core.parser.features.*;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphFeatureDefinition;
import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Way;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;


/**
 * An example showing the usage of the OpenStreetMap parser-framework.
 * The map to be parsed can be specified via the command-line options.
 *
 * @author Maximilian Luz
 */
public class ParserExample {
    private static final String DEFAULT_OSM_XML = "map.processing";


    public static void main(String[] args) throws XMLStreamException, IOException {
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
        StreetGraph streetgraph = result.streetgraph;

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

        // set the generator-indices
        int genindexBefore      = 256;
        int genindexStreetGraph = 512;

        // create the feature generators
        MapFeatureGenerator<Street> streetsGenerator = new StreetFeatureGenerator();
        StreetGraphGenerator sgGenerator      = new StreetGraphGenerator(new SimulationConfig());

        // define the features
        MapFeatureDefinition<Street> streets= new MapFeatureDefinition<>(
                "streets",
                genindexStreetGraph + 1,    // generate after StreetGraph
                streetsGenerator,
                streetgraphNodeMatcher,
                streetgraphWayMatcher
        );

        StreetGraphFeatureDefinition sg = new StreetGraphFeatureDefinition(
                "streetgraph",
                genindexStreetGraph,
                sgGenerator,
                streetgraphNodeMatcher,
                streetgraphWayMatcher
        );

        return new OSMParser.Config()
                .setGeneratorIndexUnification(genindexBefore)
                .setGeneratorIndexStreetGraph(genindexStreetGraph)
                .setStreetGraphFeatureDefinition(sg)
                .putMapFeatureDefinition(streets)
                .putWayInitializer(StreetComponent.class, new StreetComponentFactory())
                .putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
                .putRelationInitializer("restriction", new RestrictionRelationFactory())
                .createParser();
    }
}
