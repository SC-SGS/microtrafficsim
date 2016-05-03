package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Rect2d {
	public double xmin, ymin, xmax, ymax;

	public Rect2d(double xmin, double ymin, double xmax, double ymax) {
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
	}
	
	public Rect2d(Vec2d min, Vec2d max) {
		this.xmin = min.x;
		this.ymin = min.y;
		this.xmax = max.x;
		this.ymax = max.y;
	}

	public Rect2d(Rect2d other) {
		this.xmin = other.xmin;
		this.ymin = other.ymin;
		this.xmax = other.xmax;
		this.ymax = other.ymax;
	}

	public Vec2d min() {
		return new Vec2d(xmin, ymin);
	}
	
	public Vec2d max() {
		return new Vec2d(xmax, ymax);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Rect2d))
			return false;
		
		Rect2d other = (Rect2d) obj;
		
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
