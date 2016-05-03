package microtrafficsim.utils.hashing;


/**
 * HashBuilder using the FNV-1a algorithm.
 * 
 * @author Maximilian Luz
 */
public class FNVHashBuilder implements HashBuilder {
	
	public static int FNV_PRIME = 16777619;
	public static int FNV_BASE = (int) 2166136261L;
	
	private int hash;
	
	public  FNVHashBuilder() {
		this.hash = FNV_BASE;
	}

	@Override
	public void reset() {
		this.hash = FNV_BASE;
	}

	@Override
	public int getHash() {
		return hash;
	}

	@Override
	public FNVHashBuilder add(Object obj) {
		hash ^= obj != null ? obj.hashCode() : 0;
		hash *= FNV_PRIME;
		return this;
	}
}
