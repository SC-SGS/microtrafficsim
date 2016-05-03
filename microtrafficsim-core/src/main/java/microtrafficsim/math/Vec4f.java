package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;

public class Vec4f {
	public float x, y, z, w;


	public Vec4f() {
		this(0, 0, 0, 1);
	}

	public Vec4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4f(Vec2f xy, float z, float w) {
		this(xy.x, xy.y, z, w);
	}
	
	public Vec4f(Vec3f xyz, float w) {
		this(xyz.x, xyz.y, xyz.z, w);
	}

	public Vec4f(Vec4f xyzw) {
		this(xyzw.x, xyzw.y, xyzw.z, xyzw.w);
	}
	

	public Vec4f set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vec4f set(Vec2f xy, float z, float w) {
		this.x = xy.x;
		this.y = xy.y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vec4f set(Vec3f xyz, float w) {
		this.x = xyz.x;
		this.y = xyz.y;
		this.z = xyz.z;
		this.w = w;
		return this;
	}
	
	public Vec4f set(Vec4f xyzw) {
		this.x = xyzw.x;
		this.y = xyzw.y;
		this.z = xyzw.z;
		this.w = xyzw.w;
		return this;
	}
	

	public float len() {
		return (float) Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	public float len3() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vec4f normalize() {
		float abs = (float) Math.sqrt(x*x + y*y + z*z + w*w);

		x /= abs;
		y /= abs;
		z /= abs;
		w /= abs;

		return this;
	}
	
	public Vec4f normalize3() {
		float abs = (float) Math.sqrt(x*x + y*y + z*z);

		x /= abs;
		y /= abs;
		z /= abs;

		return this;
	}


	
	public Vec4f add(Vec4f v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		this.w += v.w;
		return this;
	}
	
	public Vec4f add3(Vec4f v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public Vec4f sub(Vec4f v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		this.w -= v.w;
		return this;
	}
	
	public Vec4f sub3(Vec4f v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public Vec4f mul(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		this.w *= scalar;
		return this;
	}
	
	public Vec4f mul3(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	
	public float dot(Vec4f v) {
		return this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w;
	}
	
	public float dot3(Vec4f v) {
		return this.x * v.x + this.y * v.y + this.z * v.z;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec4f))
			return false;
		
		Vec4f other = (Vec4f) obj;
		
		return this.x == other.x
				&& this.y == other.y
				&& this.z == other.z
				&& this.w == other.w;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(x)
				.add(y)
				.add(z)
				.add(w)
				.getHash();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " {" + x + ", " + y + ", " + z + ", " + w  + "}";
	}

	
	public static Vec4f normalize(Vec4f v) {
		float abs = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z + v.w * v.w);
		
		return new Vec4f(
				v.x / abs,
				v.y / abs,
				v.z / abs,
				v.w / abs
		);
	}
	
	public static Vec4f normalize3(Vec4f v) {
		float abs = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
		
		return new Vec4f(
				v.x / abs,
				v.y / abs,
				v.z / abs,
				1.f
		);
	}
	
	
	public static Vec4f add(Vec4f a, Vec4f b) {
		return new Vec4f(
				a.x + b.x,
				a.y + b.y,
				a.z + b.z,
				a.w + b.w
		);
	}
	
	public static Vec4f add3(Vec4f a, Vec4f b) {
		return new Vec4f(
				a.x + b.x,
				a.y + b.y,
				a.z + b.z,
				a.w
		);
	}
	
	public static Vec4f sub(Vec4f a, Vec4f b) {
		return new Vec4f(
				a.x - b.x,
				a.y - b.y,
				a.z - b.z,
				a.w - b.w
		);
	}
	
	public static Vec4f sub3(Vec4f a, Vec4f b) {
		return new Vec4f(
				a.x - b.x,
				a.y - b.y,
				a.z - b.z,
				a.w
		);
	}
	
	public static Vec4f mul(Vec4f v, float scalar) {
		return new Vec4f(
				v.x *= scalar,
				v.y *= scalar,
				v.z *= scalar,
				v.w *= scalar
		);
	}
	
	public static Vec4f mul3(Vec4f v, float scalar) {
		return new Vec4f(
				v.x *= scalar,
				v.y *= scalar,
				v.z *= scalar,
				v.w
		);
	}
	
	
	public static float dot(Vec4f a, Vec4f b) {
		return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
	}
	
	public static float dot3(Vec4f a, Vec4f b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}
}
