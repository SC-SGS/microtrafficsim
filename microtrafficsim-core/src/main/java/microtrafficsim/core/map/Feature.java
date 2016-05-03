package microtrafficsim.core.map;


/**
 * Groups multiple {@code FeaturePrimitive} objects of the same type to a named
 * group.
 *
 * @param <T> the type of the grouped objects
 * 
 * @author Maximilian Luz
 */
public class Feature<T extends FeaturePrimitive> {
	
	private final String name;
	private final Class<T> type;
	private T[] data;
	
	public Feature(String name, Class<T> type, T[] data) {
		this.name = name;
		this.type = type;
		this.data = data;
	}
	
	
	public String getName() {
		return name;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public T[] getData() {
		return data;
	}
}
