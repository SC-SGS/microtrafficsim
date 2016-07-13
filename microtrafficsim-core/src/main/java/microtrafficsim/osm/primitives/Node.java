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

    public Node(long id, double lat, double lon, boolean visible, Map<String, String> tags) {
        super(id, visible);
        this.lat     = lat;
        this.lon     = lon;
        this.visible = visible;
        this.tags    = tags;
    }
}
