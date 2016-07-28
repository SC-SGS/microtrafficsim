package microtrafficsim.osm.primitives;

import java.util.ArrayList;
import java.util.Map;


/**
 * Represents the OpenStreetMap {@code relation} element (e.g. xml-element).
 *
 * @author Maximilian Luz
 */
public class Relation extends Primitive {
    public ArrayList<RelationMember> members;
    public Map<String, String> tags;

    /**
     * Constructs a new Relation with the given properties.
     *
     * @param id      the id of the node.
     * @param visible the visibility of the node ({@code true} means visible).
     * @param members the members of this relation.
     * @param tags    the tags describing this relation.
     */
    public Relation(long id, boolean visible, ArrayList<RelationMember> members, Map<String, String> tags) {
        super(id, visible);
        this.members = members;
        this.tags    = tags;
    }
}
