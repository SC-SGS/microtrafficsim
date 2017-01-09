package microtrafficsim.osm.parser;

import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.base.MultiPassParserBaseEventHandler;
import microtrafficsim.osm.parser.base.ParserBase;
import microtrafficsim.osm.parser.ecs.entities.NodeEntityManager;
import microtrafficsim.osm.parser.ecs.entities.WayEntityManager;
import microtrafficsim.osm.parser.features.FeatureSystem;
import microtrafficsim.osm.parser.relations.RelationBase;
import microtrafficsim.osm.parser.relations.RelationManager;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * OpenStreetMap XML parser.
 * This framework-class provides the base to build a customized OSM parser, it
 * does not contain any predefined {@code FeatureGenerator}s or other rule-sets
 * for creating and/or modifying data. For a functional parser these rule-sets
 * should be provided using the {@code FeatureSystem}, {@code RelationManager},
 * {@code NodeEntityManager}, {@code WayEntityManager} and {@code Processor}.
 *
 * @author Maximilian Luz
 */
public class Parser {
    private static Logger logger = new EasyMarkableLogger(Parser.class);

    private FeatureSystem features;

    private NodeEntityManager nodeManager;
    private WayEntityManager  wayManager;
    private RelationManager   relations;

    private Processor processor;


    /**
     * Create a new Parser with the specified {@code Processor} which is used
     * for internal data-processing and calling of the necessary generators.
     *
     * @param processor the {@code Processor} used for internal data-processing.
     */
    public Parser(Processor processor) {
        this.processor = processor;

        features = new FeatureSystem();

        nodeManager = new NodeEntityManager();
        wayManager  = new WayEntityManager();
        relations   = new RelationManager();
    }


    /**
     * Get the {@code FeatureSystem} of this parser which is used to categorize
     * the parsed data into different Features.
     *
     * @return the {@code FeatureSystem} of this parser.
     */
    public FeatureSystem getFeatureSystem() {
        return this.features;
    }

    /**
     * Get the {@code RelationManager} of this parser which is used to parse
     * and abstract relations into objects.
     *
     * @return the {@code RelationManager} of this parser.
     */
    public RelationManager getRelationManager() {
        return this.relations;
    }

    /**
     * Get the {@code NodeEntityManager} of this parser which is used to
     * abstract the parsed OpenStreetMap node-elements into {@code
     * NodeEntities} and {@code Component}s.
     *
     * @return the {@code NodeEntityManager} of this parser.
     */
    public NodeEntityManager getNodeEntityManager() {
        return this.nodeManager;
    }

    /**
     * Get the {@code WayEntityManager} of this parser which is used to
     * abstract the parsed OpenStreetMap way-elements into {@code WayEntity}s
     * and {@code Component}s.
     *
     * @return the {@code WayEntityManager} of this parser.
     */
    public WayEntityManager getWayEntityManager() {
        return this.wayManager;
    }


    /**
     * Parse the specified OpenStreetMap XML file in multiple passes and
     * execute the parsers {@code Processor}.
     *
     * @param file the file to be parsed.
     * @throws XMLStreamException if the XML-file is malformed.
     * @throws IOException        if the specified file cannot be read.
     * @throws Exception          if any other exception occurred during processing.
     */
    public void parse(File file) throws Exception {
        logger.info("start parsing '" + file.getPath() + "'");
        DataSet datastore = extract(file);

        logger.debug("finished parsing:");
        logger.debug("\tNodes: " + datastore.nodes.size());
        logger.debug("\tWays:  " + datastore.ways.size());
        for (Class<? extends RelationBase> type : datastore.relations.getRelationTypes()) {
            logger.debug("\t" + type.getSimpleName() + ": " + datastore.relations.getAll(type).size());
        }

        logger.info("start processing");
        processor.execute(this, datastore);

        logger.info("finished");
    }


    /**
     * Extract, abstract and store the required OpenStreetMap elements from the
     * given file. The parsers {@code FeatureSystem} determines which elements
     * are required and which are not. This method runs multiple passes over
     * the given file to extract transitive dependencies.
     *
     * @param file the file to be parsed.
     * @return a {@code DataSet} object containing all required and parsed
     * elements.
     * @throws XMLStreamException if the XML-file is malformed.
     * @throws IOException        if the specified file cannot be read.
     */
    private DataSet extract(File file) throws XMLStreamException, IOException, InterruptedException {
        DataSet datastore = new DataSet();

        // initialize the ParserBase
        MultiPassParserBaseEventHandler handler
                = new MultiPassParserBaseEventHandler(datastore, features, nodeManager, wayManager, relations);

        ParserBase base = new ParserBase(handler);

        // pass 1: parse all directly specified features
        logger.info("parsing: pass 1");
        FileInputStream in = new FileInputStream(file);
        base.parse(in);
        in.close();

        // setup check for datastore change
        int elementsPrev  = 0;
        int elementsAfter = datastore.nodes.size() + datastore.ways.size() + datastore.relations.size();

        // pass 2 to n: parse indirectly needed data (until either no changes occur or all that is needed is parsed)
        for (int i = 0; (elementsPrev != elementsAfter) && (!handler.hasRequiredPrimitives()); i++) {
            logger.info("parsing: pass " + (2 + i));
            in = new FileInputStream(file);
            base.parse(in);
            in.close();

            // check if datastore has changed
            elementsPrev  = elementsAfter;
            elementsAfter = datastore.nodes.size() + datastore.ways.size() + datastore.relations.size();
        }

        if (!handler.hasRequiredPrimitives()) { logger.warn("not all required elements could be parsed"); }

        return datastore;
    }
}
