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
	
	public Way(long id, boolean visible, ArrayList<Long> nodes, Map<String, String> tags) {
		super(id, visible);
		this.nodes = nodes;
		this.tags = tags;
	}
}
