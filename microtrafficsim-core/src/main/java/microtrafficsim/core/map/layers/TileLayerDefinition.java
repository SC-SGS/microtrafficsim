package microtrafficsim.core.map.layers;


public class TileLayerDefinition {

	private final String name;
	private int index;
	private TileLayerSource source;

	public TileLayerDefinition(String name, int index, TileLayerSource source) {
		this.name = name;
		this.index = index;
		this.source = source;
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public TileLayerSource getSource() {
		return source;
	}
}
