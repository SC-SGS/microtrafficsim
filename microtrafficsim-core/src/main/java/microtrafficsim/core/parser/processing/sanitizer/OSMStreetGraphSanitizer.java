package microtrafficsim.core.parser.processing.sanitizer;

import microtrafficsim.osm.parser.features.streets.ReverseEquals;
import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.Processor;
import microtrafficsim.core.parser.processing.GraphNodeComponent;
import microtrafficsim.core.parser.processing.OSMProcessor;
import microtrafficsim.core.parser.processing.Ways;
import microtrafficsim.utils.collections.ArrayUtils;
import microtrafficsim.utils.id.LongGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Sanitizer for the StreetGraph. This sanitizer is executed during the
 * unification-process (after splitting, before merging).
 *
 * @author Maximilian Luz
 */
public class OSMStreetGraphSanitizer implements Processor {
    private static Logger logger = LoggerFactory.getLogger(OSMStreetGraphSanitizer.class);

    private FeatureDefinition streetgraph;
    private LongGenerator idgen;

    /**
     * Constructs a new sanitizer for the given properties.
     *
     * @param streetgraph the {@code FeatureDefinition} (may be a placeholder) of the street-graph, used to identify
     *                    the features belonging to the street-graph.
     * @param idgen       the id-generator used to generate new ids for the ways of the street-graph.
     */
    public OSMStreetGraphSanitizer(FeatureDefinition streetgraph, LongGenerator idgen) {
        this.streetgraph = streetgraph;
        this.idgen       = idgen;
    }


    @Override
    public void execute(Parser parser, DataSet dataset) {
        splitABAWays(dataset);
        OSMProcessor.updateGraphNodeComponents(dataset, streetgraph);
        removeDoubledWays(dataset);
        OSMProcessor.updateGraphNodeComponents(dataset, streetgraph);
        logDoubledWays(dataset);
    }


    /**
     * Split all ways going from some node {@code A} to another node {@code B} and
     * back to {@code A} on {@code B}.
     *
     * @param dataset the {@code DataSet} on which to perform this action.
     */
    private void splitABAWays(DataSet dataset) {
        ArrayList<Long>      remove = new ArrayList<>();
        ArrayList<WayEntity> add    = new ArrayList<>();

        for (WayEntity way : dataset.ways.values()) {
            if (way.nodes.length != 3)               continue;
            if (!way.features.contains(streetgraph)) continue;
            if (way.nodes[0] != way.nodes[2])        continue;

            int[] splitpoints  = {1};
            WayEntity[] splits = Ways.split(dataset, way, splitpoints, idgen);

            remove.add(way.id);
            add.addAll(Arrays.asList(splits));
        }

        dataset.ways.keySet().removeAll(remove);
        for (WayEntity split : add)
            dataset.ways.put(split.id, split);
    }

    /**
     * Remove all doubled ways in the specified {@code DataSet}.
     *
     * @param dataset the {@code DataSet} on which to perform this action.
     */
    private void removeDoubledWays(DataSet dataset) {
        HashSet<Long> remove = new HashSet<>();

        for (WayEntity way : dataset.ways.values()) {
            if (remove.contains(way.id))             continue;
            if (!way.features.contains(streetgraph)) continue;

            // get all possible matches
            HashSet<WayEntity> referenced = new HashSet<>();
            for (long ref : way.nodes) {
                GraphNodeComponent gnc = dataset.nodes.get(ref).get(GraphNodeComponent.class);
                if (gnc != null) referenced.addAll(gnc.ways);
            }

            // get matches
            HashSet<WayEntity> matches = new HashSet<>();
            for (WayEntity ref : referenced)
                if (match(way, ref)) matches.add(ref);

            // remove doubled ways, update GraphNodeComponents
            for (WayEntity match : matches) {
                remove.add(match.id);

                for (long ref : match.nodes) {
                    GraphNodeComponent gnc = dataset.nodes.get(ref).get(GraphNodeComponent.class);
                    if (gnc != null) gnc.ways.removeCompletely(match);
                }
            }
        }

        dataset.ways.keySet().removeAll(remove);
    }

    /**
     * Check if two {@code WayEntities} are equal or reverse-equal.
     *
     * @param a the first {@code WayEntity}.
     * @param b the second {@code WayEntity}.
     * @return {@code true} if {@code a} and {@code b} are equal or reverse-equal.
     */
    private boolean match(WayEntity a, WayEntity b) {
        if (a == b) return false;

        boolean reverse;

        // check nodes
        if (Arrays.equals(a.nodes, b.nodes))
            reverse = false;
        else if (ArrayUtils.reverseEquals(a.nodes, b.nodes))
            reverse = true;
        else
            return false;

        // check features
        if (!a.features.equals(b.features)) return false;

        // check components
        if (!reverse) {
            if (!a.getAll().equals(b.getAll())) return false;
        } else {
            Set<Class<? extends Component>> types = a.getAll().keySet();
            if (!types.equals(b.getAll().keySet())) return false;

            // check for reverse-equality if possible
            for (Class<? extends Component> type : types) {
                Component ca = a.get(type);

                if (ca instanceof ReverseEquals)
                    if (!((ReverseEquals) ca).reverseEquals(b.get(type))) return false;
                else
                    if (!ca.equals(b.get(type))) return false;
            }
        }

        return true;
    }

    /**
     * Check if there are still doubled ways on the specified {@code DataSet} and log them.
     *
     * @param dataset the {@code DataSet} on which to look for the doubled ways.
     */
    private void logDoubledWays(DataSet dataset) {
        HashSet<WayEntity> checked = new HashSet<>();

        for (WayEntity way : dataset.ways.values()) {
            if (checked.contains(way))               continue;
            if (!way.features.contains(streetgraph)) continue;

            // get all possible matches
            HashSet<WayEntity> referenced = new HashSet<>();
            for (long ref : way.nodes) {
                GraphNodeComponent gnc = dataset.nodes.get(ref).get(GraphNodeComponent.class);
                if (gnc != null) referenced.addAll(gnc.ways);
            }

            // get matches
            HashSet<WayEntity> matches = new HashSet<>();
            for (WayEntity ref : referenced) {
                if (ref != way && (Arrays.equals(way.nodes, ref.nodes)
                                   || ArrayUtils.reverseEquals(way.nodes, ref.nodes))) {
                    checked.add(ref);
                    matches.add(ref);
                }
            }

            // print
            if (!matches.isEmpty()) {
                StringBuilder nodes = new StringBuilder();
                for (int i = 0; i < way.nodes.length - 1; i++) {
                    nodes.append(way.nodes[i]).append(", ");
                }
                nodes.append(way.nodes[way.nodes.length - 1]);

                logger.warn("could not remove doubled ways between nodes {"
                            + nodes.toString() + "} (" + matches.size() + ")");
            }
        }
    }
}
