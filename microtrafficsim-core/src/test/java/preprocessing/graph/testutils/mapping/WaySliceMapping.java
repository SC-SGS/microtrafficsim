package preprocessing.graph.testutils.mapping;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Mapping to map a {@code WaySlice} of one {@code WayEntity} to a {@code
 * WaySlice} of another {@code WayEntity}.
 * 
 * @author Maximilian Luz
 */
public class WaySliceMapping  {
	
	public WaySlice from;
	public WaySlice to;
	
	public WaySliceMapping(WaySlice from, WaySlice to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(from)
				.add(to)
				.getHash();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WaySliceMapping))
			return false;
		
		WaySliceMapping other = (WaySliceMapping) obj;
		
		return this.from.equals(other.from)
				&& this.to.equals(other.to);
	}
}
