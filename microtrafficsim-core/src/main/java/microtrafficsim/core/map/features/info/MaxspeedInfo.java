package microtrafficsim.core.map.features.info;

import microtrafficsim.utils.hashing.FNVHashBuilder;

/**
 * Information about the speed-limit on streets.
 * 
 * @author Maximilian Luz
 */
public class MaxspeedInfo implements ReverseEquals {
	
	public static final float UNGIVEN = -1;
	public static final float NONE = -2;
	public static final float WALKING = -3;
	public static final float SIGNALS = -4;
	
	public float forward;
	public float backward;

	public MaxspeedInfo(float forward, float backward) {
		this.forward = forward;
		this.backward = backward;
	}
	
	public MaxspeedInfo(MaxspeedInfo other) {
		this.forward = other.forward;
		this.backward = other.backward;
	}
	
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(forward)
				.add(backward)
				.getHash();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MaxspeedInfo))
			return false;
		
		MaxspeedInfo other = (MaxspeedInfo) obj;
		return this.forward == other.forward
				&& this.backward == other.backward;
	}

	@Override
	public boolean reverseEquals(Object obj) {
		if (!(obj instanceof MaxspeedInfo))
			return false;
		
		MaxspeedInfo other = (MaxspeedInfo) obj;
		return this.forward == other.backward
				&& this.backward == other.forward;
	}
}
