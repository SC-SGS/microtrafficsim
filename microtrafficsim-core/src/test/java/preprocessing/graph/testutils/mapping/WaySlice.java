package preprocessing.graph.testutils.mapping;

import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Describes a Slice of a {@code WayEntity}.
 * 
 * @author Maximilian Luz
 */
public class WaySlice {
	public final WayEntity way;
	public final int start;
	public final int end;
	
	public WaySlice(WayEntity way, int start, int end) {
		this.way = way;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(way)
				.add(start)
				.add(end)
				.getHash();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WaySlice))
			return false;
		
		WaySlice other = (WaySlice) obj;
		
		return this.way == other.way
				&& this.start == other.start
				&& this.end == other.end;
	}
}
