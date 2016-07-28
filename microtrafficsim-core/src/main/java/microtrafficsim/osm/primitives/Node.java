package microtrafficsim.osm.primitives;

import java.util.Map;


/**
 * Represents the OpenStreetMap {@code node} element (e.g. xml-element).
 *
 * @author Maximilian Luz
 */
public class Node extends Primitive {
    public double lat;    // latitude
    public double lon;    // longitude
    public Map<String, String> tags;

    /**
     * Constructs a new node with the given properties.
     *
     * @param id      the id of the node.
     * @param lat     the latitude of the node.
     * @param lon     the longitude of the node
     * @param visible the visibility of the node ({@code true} means visible).
     * @param tags    the tags describing this node.
     */
    public Node(long id, double lat, double lon, boolean visible, Map<String, String> tags) {
        super(id, visible);
        this.lat     = lat;
        this.lon     = lon;
        this.visible = visible;
        this.tags    = tags;
    }
}
