package microtrafficsim.osm.primitives;


/**
 * Represents the OpenStreetMap {@code relation->member} sub-element (e.g. xml-element).
 * 
 * @author Maximilian Luz
 */
public class RelationMember {
	public Primitive.Type type;
	public long ref;
	public String role;

	public RelationMember(Primitive.Type type, long ref, String role) {
		this.type = type;
		this.ref = ref;
		this.role = role;
	}
}
