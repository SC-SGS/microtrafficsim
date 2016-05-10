package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec3f {
	public float x, y, z;


	public Vec3f() {
		this(0, 0, 0);
	}

	public Vec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f(Vec2f xy, float z) {
		this(xy.x, xy.y, z);
	}

	public Vec3f(Vec3f xyz) {
		this(xyz.x, xyz.y, xyz.z);
	}

	public Vec3f(Vec3d xyz) {
		this((float) xyz.x, (float) xyz.y, (float) xyz.z);
	}

	
	public Vec3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3f set(Vec2f xy, float z) {
		this.x = xy.x;
		this.y = xy.y;
		this.z = z;
		return this;
	}
	
	public Vec3f set(Vec3f xyz) {
		this.x = xyz.x;
		this.y = xyz.y;
		this.z = xyz.z;
		return this;
	}


	public float len() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vec3f normalize() {
		float abs = (float) Math.sqrt(x*x + y*y + z*z);

		x /= abs;
		y /= abs;
		z /= abs;

		return this;
	}
	
	
	public Vec3f add(Vec3f vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}
	
	public Vec3f sub(Vec3f vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}
	
	public Vec3f mul(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	
	public float dot(Vec3f v) {
		return this.x * v.x + this.y * v.y + this.z * v.z;
	}
	
	public Vec3f cross(Vec3f v) {
		float x = this.y * v.z - v.y * this.z;
		float y = this.z * v.x - v.z * this.x;
		float z = this.x * v.y - v.x * this.y;
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec3f))
			return false;
		
		Vec3f other = (Vec3f) obj;
		
		return this.x == other.x
				&& this.y == other.y
				&& this.z == other.z;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(x)
				.add(y)
				.add(z)
				.getHash();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " {" + x + ", " + y + ", " + z + "}";
	}
	
	public static Vec3f normalize(Vec3f v) {
		float abs = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
		
		return new Vec3f(
				v.x / abs,
				v.y / abs,
				v.z / abs
		);
	}
	
	
	public static Vec3f add(Vec3f a, Vec3f b) {
		return new Vec3f(
				a.x + b.x,
				a.y + b.y,
				a.z + b.z
		);
	}
	
	public static Vec3f sub(Vec3f a, Vec3f b) {
		return new Vec3f(
				a.x - b.x,
				a.y - b.y,
				a.z - b.z
		);
	}
	
	public static Vec3f mul(Vec3f v, float scalar) {
		return new Vec3f(
				v.x * scalar,
				v.y * scalar,
				v.z * scalar
		);
	}


	public static float dot(Vec4f a, Vec3f b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static Vec3f cross(Vec3f a, Vec3f b) {
		return new Vec3f(
				a.y * b.z - b.y * a.z,
				a.z * b.x - b.z * a.x,
				a.x * b.y - b.x * a.y
		);
	}
}
