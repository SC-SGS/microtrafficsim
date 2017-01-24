package microtrafficsim.osm.parser.base;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.osm.parser.ecs.EntityFactory;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureMatcher;
import microtrafficsim.osm.parser.relations.RelationBase;
import microtrafficsim.osm.parser.relations.RelationFactory;
import microtrafficsim.osm.primitives.Node;
import microtrafficsim.osm.primitives.Relation;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.Resettable;

import java.util.HashSet;
import java.util.Set;


/**
 * An implementation of the {@code ParserBaseEventHandler} for parsing,
 * feature-tagging and abstracting OpenStreetMap primitives in multiple passes.
 * This implementation only keeps elements tagged by the {@code FeatureMatcher}
 * and transitive dependencies of such elements.
 *
 * @author Maximilian Luz
 */
public class MultiPassParserBaseEventHandler implements ParserBaseEventHandler, Resettable {

    private DataSet        datastore;
    private FeatureMatcher matcher;

    private EntityFactory<NodeEntity, Node> nodeFactory;
    private EntityFactory<WayEntity, Way>   wayFactory;
    private RelationFactory relationFactory;

    private HashSet<Long> requiredNodes;
    private HashSet<Long> requiredWays;

    private int pass;


    /**
     * Constructs a new {@code MultiPassParserBaseEventHandler} based on the given properties.
     *
     * @param datastore       the {@code DataSet} to be used as storage.
     * @param matcher         the {@code FeatureMatcher} to select the features that should be parsed.
     * @param nodeFactory     the factory creating the new node-entities.
     * @param wayFactory      the factory creating the new way-entities.
     * @param relationFactory the factory creating the new relations
     */
    public MultiPassParserBaseEventHandler(DataSet datastore, FeatureMatcher matcher,
                                           EntityFactory<NodeEntity, Node>   nodeFactory,
                                           EntityFactory<WayEntity, Way> wayFactory, RelationFactory relationFactory) {
        this.datastore = datastore;
        this.matcher   = matcher;

        this.nodeFactory     = nodeFactory;
        this.wayFactory      = wayFactory;
        this.relationFactory = relationFactory;

        this.requiredNodes = new HashSet<>();
        this.requiredWays  = new HashSet<>();

        this.pass = 0;
    }


    @Override
    public void onPrimitiveParsed(Bounds b) {
        if (datastore.bounds == null) datastore.bounds = b;
    }

    @Override
    public void onPrimitiveParsed(Node n) {
        Set<FeatureDefinition> features;

        // early return if node is not required or already parsed
        if (pass == 0) {
            features = matcher.getFeatures(n);
            if (features == null || features.isEmpty()) return;
        } else if (requiredNodes.contains(n.id)) {
            features = new HashSet<>();
        } else {
            return;
        }

        // create entity
        NodeEntity entity = nodeFactory.create(n, features);

        // make sure required primitives are parsed
        requiredNodes.addAll(entity.getRequiredNodes());
        requiredWays.addAll(entity.getRequiredWays());

        // store
        datastore.nodes.put(entity.id, entity);
    }

    @Override
    public void onPrimitiveParsed(Way w) {
        Set<FeatureDefinition> features;

        // early return if way is not required or already parsed
        if (pass == 0) {
            features = matcher.getFeatures(w);
            if (features == null || features.isEmpty()) return;
        } else if (requiredWays.contains(w.id)) {
            features = new HashSet<>();
        } else {
            return;
        }

        // create entity
        WayEntity entity = wayFactory.create(w, features);

        // make sure required primitives are parsed
        requiredNodes.addAll(entity.getRequiredNodes());
        requiredWays.addAll(entity.getRequiredWays());

        // store
        datastore.ways.put(entity.id, entity);
    }

    @Override
    public void onPrimitiveParsed(Relation r) {
        if (pass != 0) return;

        RelationBase relation = relationFactory.create(r);

        if (relation == null) return;

        requiredNodes.addAll(relation.getRequiredNodes());
        requiredWays.addAll(relation.getRequiredWays());

        datastore.relations.add(relation);
    }


    @Override
    public void onStart() {}

    @Override
    public void onEnd() {
        requiredNodes.removeAll(datastore.nodes.keySet());
        requiredWays.removeAll(datastore.ways.keySet());
        pass++;
    }


    /**
     * Checks if all transitive dependencies of elements in the internal {@code
     * DataSet} are also contained in this.
     *
     * @return {@code true} if all required transitive dependencies have been
     * parsed, {@code false} otherwise.
     */
    public boolean hasRequiredPrimitives() {
        return requiredNodes.isEmpty() && requiredWays.isEmpty();
    }


    /**
     * Resets the internal storage except for the {@code DataSet}, which has to
     * be cleared manually.
     */
    @Override
    public void reset() {
        pass = 0;
        requiredNodes.clear();
        requiredWays.clear();
    }
}
