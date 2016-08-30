package preprocessing.graph.testutils;

import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.processing.sanitizer.OSMDataSetSanitizer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.core.parser.processing.OSMProcessor;
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
 * Test-data for the {@code GraphConsistencyTest}, contains {@code DataSets}
 * before and after the unification-process.
 *
 * @author Maximilian Luz
 */
public class TestData {

    public final DataSet stage1;
    public final DataSet stage2;

    private TestData(DataSet stage1, DataSet stage2) {
        this.stage1      = stage1;
        this.stage2      = stage2;
    }


    /**
     * Parse the {@code TestData} from the given {@code File}.
     *
     * @param osmxml              the {@code File} to parse the data from.
     * @param genindexBefore      the 'before' generator-index for the parser.
     * @param genindexStreetGraph the 'street-graph' generator index for the
     *                            parser.
     * @throws XMLStreamException relayed from the parser.
     * @throws IOException        relayed from the parser.
     * @return returns the parsed {@code TestData}.
     */
    public static TestData parse(File osmxml, int genindexBefore, int genindexStreetGraph, Predicate<Way> sgmatcher)
            throws XMLStreamException, IOException {

        DataSetCloneExtractor s1extractor = new DataSetCloneExtractor();
        DataSetCloneExtractor s2extractor = new DataSetCloneExtractor();
        StreetGraphGenerator  sggen       = new StreetGraphGenerator(new SimulationConfig());

        Predicate<Node> nodematcher = (Node n) -> false;

        FeatureDefinition stage1 = new FeatureDefinition("stage_1",
                                                         genindexBefore,                // before unification
                                                         s1extractor,
                                                         nodematcher,
                                                         sgmatcher);

        FeatureDefinition stage2 = new FeatureDefinition("stage_2",
                                                         genindexBefore + 1,            // after unification
                                                         s2extractor,
                                                         nodematcher,
                                                         sgmatcher);

        FeatureDefinition streetgraph = new FeatureDefinition("streetgraph",
                                                              genindexStreetGraph,      // street-graph
                                                              sggen,
                                                              nodematcher,
                                                              sgmatcher);

        Parser parser = new Parser(new OSMProcessor(genindexBefore, genindexStreetGraph, OSMDataSetSanitizer.BoundaryMgmt.NONE));
        parser.getWayEntityManager().putInitializer(StreetComponent.class, new StreetComponentFactory());
        parser.getWayEntityManager().putInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory());
        parser.getRelationManager().putFactory("restriction", new RestrictionRelationFactory());

        parser.getFeatureSystem().putFeature(stage1);
        parser.getFeatureSystem().putFeature(stage2);
        parser.getFeatureSystem().putFeature(streetgraph);

        parser.parse(osmxml);

        return new TestData(s1extractor.getDataSet(), s2extractor.getDataSet());
    }
}
