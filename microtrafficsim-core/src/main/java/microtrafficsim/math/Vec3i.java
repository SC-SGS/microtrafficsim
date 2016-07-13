package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec3i {
    public int x, y, z;


    public Vec3i() {
        this(0, 0, 0);
    }

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(Vec2i xy, int z) {
        this(xy.x, xy.y, z);
    }

    public Vec3i(Vec3i xyz) {
        this(xyz.x, xyz.y, xyz.z);
    }

    public Vec3i set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3i set(Vec2i xy, int z) {
        this.x = xy.x;
        this.y = xy.y;
        this.z = z;
        return this;
    }

    public Vec3i set(Vec3i xyz) {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        return this;
    }


    public float len() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }


    public Vec3i add(Vec3i vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3i sub(Vec3i vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3i mul(int scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public int dot(Vec3i v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vec3i cross(Vec3i v) {
        int x = this.y * v.z - v.y * this.z;
        int y = this.z * v.x - v.z * this.x;
        int z = this.x * v.y - v.x * this.y;

        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }


    public static Vec3i add(Vec3i a, Vec3i b) {
        return new Vec3i(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3i sub(Vec3i a, Vec3i b) {
        return new Vec3i(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vec3i mul(Vec3i v, int scalar) {
        return new Vec3i(v.x * scalar, v.y * scalar, v.z * scalar);
    }

    public static float dot(Vec4f a, Vec3i b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vec3i cross(Vec3i a, Vec3i b) {
        return new Vec3i(a.y * b.z - b.y * a.z, a.z * b.x - b.z * a.x, a.x * b.y - b.x * a.y);
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3i)) return false;

        Vec3i other = (Vec3i) obj;
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
}
