package microtrafficsim.osm.primitives;


/**
 * Base class for all OpenStreetMap primitives.
 * 
 * @author Maximilian Luz
 */
public abstract class Primitive {
	public enum Type {
		NODE,
		WAY,
		RELATION
	}
	
	
	public long id;
	public boolean visible;

	public Primitive(long id, boolean visible) {
		this.id = id;
		this.visible = visible;
	}
}
