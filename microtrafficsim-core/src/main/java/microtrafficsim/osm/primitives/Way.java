package microtrafficsim.osm.primitives;

import java.util.ArrayList;
import java.util.Map;


/**
 * Represents the OpenStreetMap {@code way} element (e.g. xml-element).
 *
 * @author Maximilian Luz
 */
public class Way extends Primitive {
    public ArrayList<Long> nodes;
    public Map<String, String> tags;

    /**
     * Constructs a new way using the given properties.
     *
     * @param id      the id of the way.
     * @param visible the visibility of the node ({@code true} means visible).
     * @param nodes   the nodes (in order) that are a part of this way.
     * @param tags    the tags describing this way.
     */
    public Way(long id, boolean visible, ArrayList<Long> nodes, Map<String, String> tags) {
        super(id, visible);
        this.nodes = nodes;
        this.tags  = tags;
    }
}
