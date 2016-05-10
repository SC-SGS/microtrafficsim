package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec2i {
	public int x, y;


	public Vec2i() {
		this(0, 0);
	}

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vec2i(Vec2i xy) {
		this(xy.x, xy.y);
	}
	
	public Vec2i(Vec2f xy) {
		this.x = (int) xy.x;
		this.y = (int) xy.y;
	}
	
	
	public Vec2i set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vec2i set(Vec2i xy) {
		this.x = xy.x;
		this.y = xy.y;
		return this;
	}
	
	public Vec2i set(Vec2f xy) {
		this.x = (int) xy.x;
		this.y = (int) xy.y;
		return this;
	}


	public float len() {
		return (float) Math.hypot(x, y);
	}
	
	
	public Vec2i add(Vec2i v) {
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vec2i sub(Vec2i v) {
		this.x -= v.x;
		this.y -= v.y;
		return this;
	}
	
	public Vec2i mul(int scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}


	public int dot(Vec2i v) {
		return this.x * v.x + this.y * v.y;
	}
	
	public int cross(Vec2i v) {
		return this.x * v.y - this.y * v.x;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec2i))
			return false;
		
		Vec2i other = (Vec2i) obj;
		
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


	public static Vec2f normalize(Vec2i v) {
		float abs = (float) Math.hypot(v.x, v.y);
		return new Vec2f(v.x / abs, v.y / abs);
	}
	
	
	public static Vec2i add(Vec2i a, Vec2i b) {
		return new Vec2i(a.x + b.x, a.y + b.y);
	}
	
	public static Vec2i sub(Vec2i a, Vec2i b) {
		return new Vec2i(a.x - b.x, a.y - b.y);
	}
	
	public static Vec2i mul(Vec2i v, int scalar) {
		return new Vec2i(v.x * scalar, v.y * scalar);
	}


	public static float dot(Vec2i a, Vec2i b) {
		return a.x * b.x + a.y * b.y;
	}
	
	public static float cross(Vec2i a, Vec2i b) {
		return a.x * b.y - a.y * b.x;
	}
}
