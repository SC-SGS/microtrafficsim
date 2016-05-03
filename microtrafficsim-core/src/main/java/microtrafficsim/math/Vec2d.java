package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec2d {
	public double x, y;


	public Vec2d() {
		this(0, 0);
	}

	public Vec2d(Vec2d xy) {
		this(xy.x, xy.y);
	}

	public Vec2d(Vec2i xy) {
		this(xy.x, xy.y);
	}

	public Vec2d(double x, double y) {
		this.x = x;
		this.y = y;
	}


	public Vec2d set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vec2d set(Vec2d xy) {
		this.x = xy.x;
		this.y = xy.y;
		return this;
	}
	
	public Vec2d set(Vec2i xy) {
		this.x = xy.x;
		this.y = xy.y;
		return this;
	}


	public float len() {
		return (float) Math.hypot(x, y);
	}
	
	public Vec2d normalize() {
		double abs = Math.hypot(x, y);

		x /= abs;
		y /= abs;

		return this;
	}
	
	
	public Vec2d add(Vec2d v) {
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vec2d sub(Vec2d v) {
		this.x -= v.x;
		this.y -= v.y;
		return this;
	}
	
	public Vec2d mul(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}
	
	
	public double dot(Vec2d v) {
		return this.x * v.x + this.y * v.y;
	}
	
	public double cross(Vec2d v) {
		return this.x * v.y - this.y * v.x;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec2d))
			return false;
		
		Vec2d other = (Vec2d) obj;
		
		return this.x == other.x && this.y == other.y;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(x)
				.add(y)
				.getHash();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " {" + x + ", " + y + "}";
	}
	
	
	public static Vec2d normalize(Vec2d v) {
		double abs = Math.hypot(v.x, v.y);
		return new Vec2d(v.x / abs, v.y / abs);
	}


	public static Vec2d add(Vec2d a, Vec2d b) {
		return new Vec2d(a.x + b.x, a.y + b.y);
	}
	
	public static Vec2d sub(Vec2d a, Vec2d b) {
		return new Vec2d(a.x - b.x, a.y - b.y);
	}
	
	public static Vec2d mul(Vec2d v, double scalar) {
		return new Vec2d(v.x * scalar, v.y * scalar);
	}


	public static double dot(Vec2d a, Vec2d b) {
		return a.x * b.x + a.y * b.y;
	}
	
	public static double cross(Vec2d a, Vec2d b) {
		return a.x * b.y - a.y * b.x;
	}
}
