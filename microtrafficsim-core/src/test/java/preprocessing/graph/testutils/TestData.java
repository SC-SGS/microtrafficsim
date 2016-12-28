package preprocessing.graph.testutils;

import microtrafficsim.core.parser.features.streetgraph.StreetGraphGenerator;
import microtrafficsim.core.parser.processing.OSMProcessor;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureDependency;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
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

    public final FeatureDefinition streetgraph;
    public final DataSet stage1;
    public final DataSet stage2;

    private TestData(FeatureDefinition streetgraph, DataSet stage1, DataSet stage2) {
        this.streetgraph = streetgraph;
        this.stage1      = stage1;
        this.stage2      = stage2;
    }


    /**
     * Parse the {@code TestData} from the given {@code File}.
     *
     * @param osmxml              the {@code File} to parse the data from.
     * @throws XMLStreamException relayed from the parser.
     * @throws IOException        relayed from the parser.
     * @throws Exception          relayed from the parser.
     * @return returns the parsed {@code TestData}.
     */
    public static TestData parse(File osmxml, Predicate<Way> sgmatcher) throws Exception {
        DataSetCloneExtractor s1extractor = new DataSetCloneExtractor("stage 1");
        DataSetCloneExtractor s2extractor = new DataSetCloneExtractor("stage 2");
        StreetGraphGenerator  sggen       = new StreetGraphGenerator(new SimulationConfig());

        // requires to be generated before unification
        FeatureDependency stage1dep = new FeatureDependency(null, OSMProcessor.PLACEHOLDER_UNIFICATION);
        FeatureDefinition stage1 = new FeatureDefinition("stage_1", stage1dep, s1extractor, n -> false, sgmatcher);

        // depends on unification, will be created after unification
        FeatureDependency stage2dep = new FeatureDependency(OSMProcessor.PLACEHOLDER_UNIFICATION, null);
        FeatureDefinition stage2 = new FeatureDefinition("stage_2", stage2dep, s2extractor, n -> false, sgmatcher);

        // streetgraph, depends on unification and previous, will be created after unification
        FeatureDependency sgdep = new FeatureDependency(OSMProcessor.PLACEHOLDER_UNIFICATION, null);
        sgdep.addRequires(stage1);
        sgdep.addRequires(stage2);
        FeatureDefinition streetgraph = new FeatureDefinition("streetgraph", sgdep, sggen, n -> false, sgmatcher);

        FeatureGenerator.Properties properties = new FeatureGenerator.Properties();
        properties.bounds = FeatureGenerator.Properties.BoundaryManagement.NONE;

        Parser parser = new Parser(new OSMProcessor(properties, streetgraph));
        parser.getWayEntityManager().putInitializer(StreetComponent.class, new StreetComponentFactory());
        parser.getWayEntityManager().putInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory());
        parser.getRelationManager().putFactory("restriction", new RestrictionRelationFactory());

        parser.getFeatureSystem().putFeature(stage1);
        parser.getFeatureSystem().putFeature(stage2);
        parser.getFeatureSystem().putFeature(streetgraph);
        parser.getFeatureSystem().putFeature(OSMProcessor.PLACEHOLDER_UNIFICATION);

        parser.parse(osmxml);

        return new TestData(streetgraph, s1extractor.getDataSet(), s2extractor.getDataSet());
    }
}
