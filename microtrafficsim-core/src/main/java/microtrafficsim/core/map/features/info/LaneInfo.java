package microtrafficsim.core.map.features.info;

import microtrafficsim.utils.hashing.FNVHashBuilder;

/**
 * Information about the number of lanes on a street.
 * 
 * @author Maximilian Luz
 */
public class LaneInfo implements ReverseEquals {
	
	public static final int UNGIVEN = -1;
	
	public int sum;
	public int forward;
	public int backward;
	
	public LaneInfo(int sum, int forward, int backward) {
		this.sum = sum;
		this.forward = forward;
		this.backward = backward;
	}
	
	public LaneInfo(LaneInfo other) {
		this.sum = other.sum;
		this.forward = other.forward;
		this.backward = other.backward;
	}
	
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(sum)
				.add(forward)
				.add(backward)
				.getHash();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LaneInfo))
			return false;
		
		LaneInfo other = (LaneInfo) obj;
		return this.sum == other.sum
				&& this.forward == other.forward
				&& this.backward == other.backward;
	}

	@Override
	public boolean reverseEquals(Object obj) {
		if (!(obj instanceof LaneInfo))
			return false;
		
		LaneInfo other = (LaneInfo) obj;
		return this.sum == other.sum
				&& this.forward == other.backward
				&& this.backward == other.forward;
	}
}
