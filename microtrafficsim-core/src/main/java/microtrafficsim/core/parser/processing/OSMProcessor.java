package microtrafficsim.core.parser.processing;

import microtrafficsim.osm.parser.features.streets.info.OnewayInfo;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureSystem;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.Processor;
import microtrafficsim.core.parser.processing.Ways.MergePoint;
import microtrafficsim.core.parser.processing.sanitizer.OSMDataSetSanitizer;
import microtrafficsim.core.parser.processing.sanitizer.OSMStreetGraphSanitizer;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation;
import microtrafficsim.osm.primitives.Primitive;
import microtrafficsim.utils.collections.ArrayUtils;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.id.LongIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Preprocessing unit for the OpenStreetMap data used in simulation and
 * visualization. Responsible for unified simulation and visualization data and
 * calling the generator-functions for these.
 * <p>
 * The OSMProcessor requires two generator-indices {@code idxBefore} and {@code
 * idxStreetGraph}. With these values one can specify when Features are
 * generated in relation to the unification process: Features with a
 * generator-index less than or equal to {@code idxBefore} are generated before
 * the unification-process is executed. Features with a generator-index greater
 * than {@code idxBefore} are generated after the unification process. Features
 * with a generator-index of {@code idxStreetGraph} are generated next. They are
 * assumed to be direct parts of the StreetGraph and thus must fulfill certain
 * requirements (eg. have a StreetComponent). At last Features with a
 * generator-index greater than {@code idxStreetGraph} are generated.
 * </p>
 *
 * @author Maximilian Luz
 */
public class OSMProcessor implements Processor {
    private static Logger logger = LoggerFactory.getLogger(OSMProcessor.class);

    private int idxBefore;
    private int idxStreetGraph;

    private LongIDGenerator wayIdGenerator;

    private Processor datasetSanitizer;
    private Processor streetgraphSanitizer;


    /**
     * Creates a new {@code OSMProcessor} where {@code idxBefore} and {@code idxStreetGraph}
     * are the generator-indices described in the classes JavaDoc-comment.
     * <p>
     * This call is similar to {@link
     * OSMProcessor#OSMProcessor(int, int, OSMDataSetSanitizer.BoundaryMgmt, LongIDGenerator)
     * OSMProcessor(idxBefore, idxStreetGraph, new BasicLongIDGenerator())
     * }
     * </p>
     *
     * @param idxBefore      the maximum generator-index of features
     *                       generated before the unification process.
     * @param idxStreetGraph the generator-index of the street-graph.
     * @param bounds         the method used to handle map-boundaries.
     */
    public OSMProcessor(int idxBefore, int idxStreetGraph, OSMDataSetSanitizer.BoundaryMgmt bounds) {
        this(idxBefore, idxStreetGraph, bounds, new BasicLongIDGenerator());
    }

    /**
     * Creates a new {@code OSMProcessor} where {@code idxBefore} and {@code idxStreetGraph}
     * are the generator-indices described in the classes JavaDoc-comment.
     * <p>
     * This call is similar to {@link
     * OSMProcessor#OSMProcessor(int, int, LongIDGenerator, Processor, Processor)
     * OSMProcessor(idxBefore, idxStreetGraph, wayIdGenerator, new
     * OSMDataSetSanizier(), new OSMStreetGraphSanitizer(idxStreetGraph, wayIdGenerator))
     * }
     * </p>
     *
     * @param idxBefore      the maximum generator-index of features
     *                       generated before the unification process.
     * @param idxStreetGraph the generator-index of the street-graph.
     * @param wayIdGenerator the ID-generator used for new way-IDs.
     */
    public OSMProcessor(int idxBefore, int idxStreetGraph, OSMDataSetSanitizer.BoundaryMgmt bounds,
                        LongIDGenerator wayIdGenerator) {
        this(idxBefore, idxStreetGraph, wayIdGenerator, new OSMDataSetSanitizer(bounds),
             new OSMStreetGraphSanitizer(idxStreetGraph, wayIdGenerator));
    }

    /**
     * Creates a new {@code OSMProcessor} where {@code idxBefore} and {@code idxStreetGraph}
     * are the generator-indices described in the classes JavaDoc-comment.
     * <p>
     * The {@code wayIdGenerator} is used to create new IDs for the unified
     * ways.
     * </p>
     *
     * @param idxBefore            the maximum generator-index of features generated before
     *                             the unification process.
     * @param idxStreetGraph       the generator-index of the street-graph.
     * @param wayIdGenerator       the ID-generator used for new way-IDs.
     * @param datasetSanitizer     the Processor used for sanitizing the data-set.
     * @param streetgraphSanitizer the Processor used for sanitizing the
     *                             street-graph during the unification-process.
     */
    public OSMProcessor(int idxBefore, int idxStreetGraph, LongIDGenerator wayIdGenerator, Processor datasetSanitizer,
                        Processor streetgraphSanitizer) {
        if (idxBefore >= idxStreetGraph)
            throw new IllegalArgumentException("idxBefore must not be greater than or equal to idxStreetGraph");

        this.idxBefore            = idxBefore;
        this.idxStreetGraph       = idxStreetGraph;
        this.wayIdGenerator       = wayIdGenerator;
        this.datasetSanitizer     = datasetSanitizer;
        this.streetgraphSanitizer = streetgraphSanitizer;
    }


    /**
     * Sets up the GraphNodeComponents on street-graph related nodes required for
     * various processing steps
     *
     * @param dataset        the DataSet on which to execute this step on.
     * @param idxStreetGraph the generator-index of the street-graph.
     */
    public static void setupGraphNodeComponents(DataSet dataset, int idxStreetGraph) {
        for (WayEntity way : dataset.ways.values()) {
            if (!FeatureDefinition.hasGeneratorIndex(way.features, idxStreetGraph)) continue;

            for (long ref : way.nodes) {
                NodeEntity node = dataset.nodes.get(ref);

                if (node == null) throw new RuntimeException("node with id " + ref + " needed but not found");

                GraphNodeComponent uc = node.get(GraphNodeComponent.class);
                if (uc == null) {
                    uc = new GraphNodeComponent(node);
                    node.set(GraphNodeComponent.class, uc);
                }

                uc.ways.add(way);
            }
        }
    }

    /**
     * Updates the GraphNodeComponents on street-graph related nodes required for
     * various processing steps.
     *
     * @param dataset        the DataSet on which to execute this step on.
     * @param idxStreetGraph the generator-index of the street-graph.
     */
    public static void updateGraphNodeComponents(DataSet dataset, int idxStreetGraph) {
        for (NodeEntity node : dataset.nodes.values()) {
            GraphNodeComponent uc = node.get(GraphNodeComponent.class);
            if (uc == null) continue;

            uc.ways.clear();
        }

        setupGraphNodeComponents(dataset, idxStreetGraph);
    }

    /**
     * Removes all GraphNodeComponents required for various processing steps.
     *
     * @param dataset the DataSet on which to execute this step on.
     */
    public static void removeGraphNodeComponents(DataSet dataset) {
        for (NodeEntity node : dataset.nodes.values()) {
            node.remove(GraphNodeComponent.class);
        }
    }


    /**
     * Sets up the GraphWayComponents including the Street-Connectors. This step
     * requires the GraphNodeComponents to be set up. Streets with one-way-type
     * {@code REVERSIBLE} are handled as if the one-way-type would be {@code NO}.
     *
     * @param dataset the DataSet on which to perform this action on.
     */
    private void setupGraphWayComponents(DataSet dataset) {

        // assure all street-graph/street related ways have GraphWayComponents, create cyclic connectors
        for (WayEntity way : dataset.ways.values()) {
            StreetComponent sc = way.get(StreetComponent.class);
            if (sc == null) continue;

            GraphWayComponent gwc = new GraphWayComponent(way);
            way.set(GraphWayComponent.class, gwc);

            // cyclic connectors
            if (way.nodes[0] == way.nodes[way.nodes.length - 1]) {
                if (sc.oneway == OnewayInfo.NO || sc.oneway == OnewayInfo.REVERSIBLE) {
                    gwc.cyclicEndToStart = true;
                    gwc.cyclicStartToEnd = true;

                } else if (sc.oneway == OnewayInfo.FORWARD) {
                    gwc.cyclicEndToStart = true;
                    gwc.cyclicStartToEnd = false;

                } else if (sc.oneway == OnewayInfo.BACKWARD) {
                    gwc.cyclicEndToStart = false;
                    gwc.cyclicStartToEnd = true;
                }
            }
        }

        // create connectors between all streets
        for (NodeEntity node : dataset.nodes.values()) {
            GraphNodeComponent gnc = node.get(GraphNodeComponent.class);
            if (gnc == null || gnc.ways.size() == 0) continue;

            // multiple ways: add all connectors (including u-turns)
            if (gnc.ways.size() > 1) {
                for (WayEntity from : gnc.ways) {
                    for (WayEntity to : gnc.ways) {
                        Connectors.add(Connectors.create(node, from, to));
                    }
                }

                // single way: add u-turn connectors if node is at beginning or end of way
            } else if (gnc.ways.count() == 1) {
                for (WayEntity way : gnc.ways) {
                    if ((way.nodes[0] == node.id) || (way.nodes[way.nodes.length - 1] == node.id)) {
                        Connectors.add(Connectors.create(node, way, way));
                    }
                }
            }

            /* NOTE:
             *	Self-intersections are automatically assumed to be fully interconnected,
             *	no need to add connectors here (see 'splitWay' method).
             */
        }
    }


    /**
     * Applies the restriction-relations stored in the given {@code DataSet} on it.
     * If a restriction cannot be applied because of missing Ways or Nodes, it will
     * be skipped.
     *
     * @param dataset the {@code DataSet} on which this action should be performed.
     */
    private void applyRestrictionRelations(DataSet dataset) {
        for (RestrictionRelation r : dataset.relations.getAll(RestrictionRelation.class).values()) {
            if (r.viaType != Primitive.Type.NODE || r.via.size() != 1) continue;

            NodeEntity via = dataset.nodes.get(r.via.get(0));
            if (via == null) continue;

            if (r.restriction.isOnlyType()) {    // 'only'-type
                for (long rFrom : r.from) {
                    WayEntity from = dataset.ways.get(rFrom);
                    if (from == null) continue;

                    GraphWayComponent gwc = from.get(GraphWayComponent.class);
                    if (gwc == null) continue;

                    // create all connectors to be retained
                    HashSet<Connector> connectors = new HashSet<>(r.to.size());
                    for (long rTo : r.to) {
                        WayEntity to = dataset.ways.get(rTo);
                        if (to == null) continue;

                        connectors.add(new Connector(via, from, to));
                    }

                    // get all connectors to be removed
                    HashSet<Connector> remove = new HashSet<>();
                    for (Connector c : gwc.from)
                        if (c.via.id == via.id && !connectors.contains(c)) remove.add(c);

                    // remove the selected connectors
                    for (Connector c : remove)
                        Connectors.remove(c);
                }

            } else {    // 'no'-type
                for (long rFrom : r.from) {
                    WayEntity from = dataset.ways.get(rFrom);
                    if (from == null) continue;

                    for (long rTo : r.to) {
                        WayEntity to = dataset.ways.get(rTo);
                        if (to == null) continue;

                        Connectors.remove(new Connector(via, from, to));
                    }
                }
            }
        }
    }


    /**
     * Splits the Ways relevant for the StreetGraph on crossings.
     *
     * @param dataset the DataSet on which to execute this step on.
     */
    private void unifySplit(DataSet dataset) {
        HashMap<Long, WayEntity> ways = new HashMap<>();

        for (WayEntity way : dataset.ways.values()) {

            // if not part of streetgraph just change the id
            if (!FeatureDefinition.hasGeneratorIndex(way.features, idxStreetGraph)) {
                way.id = wayIdGenerator.next();
                ways.put(way.id, way);

                // connectors reference the object directly and thus need not be updated
                continue;
            }

            // get indices of split nodes
            List<Integer> splitpoints = new ArrayList<>();
            for (int i = 1; i < way.nodes.length - 1; i++) {
                Long ref = way.nodes[i];

                // if another way references this node, split the way
                if (dataset.nodes.get(ref).get(GraphNodeComponent.class).ways.count() > 1) { splitpoints.add(i); }
            }

            // split the way
            WayEntity[] splits = Ways.split(dataset, way, ArrayUtils.toArray(splitpoints, null), wayIdGenerator);
            for (WayEntity split : splits) {
                ways.put(split.id, split);
            }
        }

        dataset.ways = ways;
    }

    /**
     * Merges adjacent Ways if they are equal in a simulation-logical way.
     *
     * @param dataset the DataSet on which to execute this step on.
     */
    private void unifyMerge(DataSet dataset) {
        for (NodeEntity node : dataset.nodes.values()) {
            GraphNodeComponent uc = node.get(GraphNodeComponent.class);
            if (uc == null) continue;

            if (uc.ways.size() == 2 && uc.ways.count() == 2) {
                Iterator<WayEntity> it = uc.ways.iterator();
                WayEntity           w1 = it.next();
                WayEntity           w2 = it.next();

                // if a merge is possible, merge
                MergePoint joint = Ways.mergepoint(dataset, node, w1, w2);
                if (joint != null) Ways.merge(dataset, joint);
            }
        }
    }


    @Override
    public void execute(Parser parser, DataSet dataset) {
        FeatureSystem featuresys = parser.getFeatureSystem();

        // sanitize dataset
        datasetSanitizer.execute(parser, dataset);

        // create connectors, apply restrictions
        setupGraphNodeComponents(dataset, idxStreetGraph);
        setupGraphWayComponents(dataset);
        applyRestrictionRelations(dataset);

        // generate first features
        featuresys.generateAllFeatures(dataset, Integer.MIN_VALUE, idxBefore);

        // unify StreetGraph: setup
        logger.debug("dataset before unification: " + dataset.nodes.size() + " nodes, " + dataset.ways.size()
                     + " ways");
        updateGraphNodeComponents(dataset, idxStreetGraph);

        // unify StreetGraph: split
        unifySplit(dataset);
        updateGraphNodeComponents(dataset, idxStreetGraph);

        // unify StreetGraph: sanitize
        streetgraphSanitizer.execute(parser, dataset);
        updateGraphNodeComponents(dataset, idxStreetGraph);

        // unify: StreetGraph merge
        unifyMerge(dataset);
        removeGraphNodeComponents(dataset);
        logger.debug("dataset after unification:  " + dataset.nodes.size() + " nodes, " + dataset.ways.size()
                     + " ways");

        // generate remaining features
        featuresys.generateAllFeatures(dataset, idxBefore + 1, idxStreetGraph - 1);
        featuresys.generateAllFeatures(dataset, idxStreetGraph);
        featuresys.generateAllFeatures(dataset, idxStreetGraph + 1, Integer.MAX_VALUE);
    }
}
