package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Rect2i {
	public int xmin, ymin, xmax, ymax;

	public Rect2i(int xmin, int ymin, int xmax, int ymax) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
	}

	public Rect2i(Vec2i min, Vec2i max) {
		this.xmin = min.x;
		this.ymin = min.y;
		this.xmax = max.x;
		this.ymax = max.y;
	}

	public Rect2i(Rect2i other) {
		this.xmin = other.xmin;
		this.ymin = other.ymin;
		this.xmax = other.xmax;
		this.ymax = other.ymax;
	}

	public Vec2i min() {
		return new Vec2i(xmin, ymin);
	}
	
	public Vec2i max() {
		return new Vec2i(xmax, ymax);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Rect2i))
			return false;
		
		Rect2i other = (Rect2i) obj;
		
		return this.xmin == other.xmin
				&& this.ymin == other.ymin
				&& this.xmax == other.xmax
				&& this.ymax == other.ymax;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(xmin)
				.add(ymin)
				.add(xmax)
				.add(ymax)
				.getHash();
	}
	
	@Override
	public String toString() {
		return this.getClass() + " {" + xmin + ", " + ymin + ", " + xmax + ", " + ymax + "}";
	}
}
