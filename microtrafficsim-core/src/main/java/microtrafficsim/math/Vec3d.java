package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec3d {
	public double x, y, z;


	public Vec3d() {
		this(0, 0, 0);
	}

	public Vec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3d(Vec2f xy, double z) {
		this(xy.x, xy.y, z);
	}

	public Vec3d(Vec3d xyz) {
		this(xyz.x, xyz.y, xyz.z);
	}

	public Vec3d(Vec3f xyz) {
		this(xyz.x, xyz.y, xyz.z);
	}

	
	public Vec3d set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3d set(Vec2f xy, double z) {
		this.x = xy.x;
		this.y = xy.y;
		this.z = z;
		return this;
	}
	
	public Vec3d set(Vec3d xyz) {
		this.x = xyz.x;
		this.y = xyz.y;
		this.z = xyz.z;
		return this;
	}

	public Vec3d set(Vec3f xyz) {
		this.x = xyz.x;
		this.y = xyz.y;
		this.z = xyz.z;
		return this;
	}


	public double len() {
		return (double) Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vec3d normalize() {
		double abs = (double) Math.sqrt(x*x + y*y + z*z);

		x /= abs;
		y /= abs;
		z /= abs;

		return this;
	}
	
	
	public Vec3d add(Vec3d vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}
	
	public Vec3d sub(Vec3d vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}
	
	public Vec3d mul(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	
	public double dot(Vec3d v) {
		return this.x * v.x + this.y * v.y + this.z * v.z;
	}
	
	public Vec3d cross(Vec3d v) {
		double x = this.y * v.z - v.y * this.z;
		double y = this.z * v.x - v.z * this.x;
		double z = this.x * v.y - v.x * this.y;
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec3d))
			return false;
		
		Vec3d other = (Vec3d) obj;
		
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
	
	public static Vec3d normalize(Vec3d v) {
		double abs = (double) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
		
		return new Vec3d(
				v.x / abs,
				v.y / abs,
				v.z / abs
		);
	}
	
	
	public static Vec3d add(Vec3d a, Vec3d b) {
		return new Vec3d(
				a.x + b.x,
				a.y + b.y,
				a.z + b.z
		);
	}
	
	public static Vec3d sub(Vec3d a, Vec3d b) {
		return new Vec3d(
				a.x - b.x,
				a.y - b.y,
				a.z - b.z
		);
	}
	
	public static Vec3d mul(Vec3d v, double scalar) {
		return new Vec3d(
				v.x * scalar,
				v.y * scalar,
				v.z * scalar
		);
	}


	public static double dot(Vec4f a, Vec3d b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static Vec3d cross(Vec3d a, Vec3d b) {
		return new Vec3d(
				a.y * b.z - b.y * a.z,
				a.z * b.x - b.z * a.x,
				a.x * b.y - b.x * a.y
		);
	}
}
