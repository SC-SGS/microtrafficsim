package microtrafficsim.osm.primitives;


/**
 * Base class for all OpenStreetMap primitives.
 *
 * @author Maximilian Luz
 */
public abstract class Primitive {
    public long    id;
    public boolean visible;

    /**
     * Constructs a new primitive with the given properties.
     *
     * @param id      the id of the node.
     * @param visible the visibility of the node ({@code true} means visible).
     */
    public Primitive(long id, boolean visible) {
        this.id      = id;
        this.visible = visible;
    }

    /**
     * Basic types of primitives.
     */
    public enum Type { NODE, WAY, RELATION }
}
