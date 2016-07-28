package microtrafficsim.osm.primitives;


/**
 * Represents the OpenStreetMap {@code relation->member} sub-element (e.g. xml-element).
 *
 * @author Maximilian Luz
 */
public class RelationMember {
    public Primitive.Type type;
    public long           ref;
    public String         role;

    /**
     * Constructs a new relation-member for the given primitive and role.
     *
     * @param type the type of the primitive (member).
     * @param ref  the reference (id) of the primitive.
     * @param role the role the primitive described by {@code type} and {@code ref} has in this relation.
     */
    public RelationMember(Primitive.Type type, long ref, String role) {
        this.type = type;
        this.ref  = ref;
        this.role = role;
    }
}
