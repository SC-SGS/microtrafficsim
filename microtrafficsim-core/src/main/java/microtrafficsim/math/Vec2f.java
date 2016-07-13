package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Vec2f {
    public float x, y;


    public Vec2f() {
        this(0, 0);
    }

    public Vec2f(Vec2f xy) {
        this(xy.x, xy.y);
    }

    public Vec2f(Vec2i xy) {
        this(xy.x, xy.y);
    }

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(Vec3f xyz) {
        this.x = xyz.x;
        this.y = xyz.y;
    }

    public Vec2f(Vec4f xyzw) {
        this.x = xyzw.x;
        this.y = xyzw.y;
    }

    public Vec2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2f set(Vec2f xy) {
        this.x = xy.x;
        this.y = xy.y;
        return this;
    }

    public Vec2f set(Vec2i xy) {
        this.x = xy.x;
        this.y = xy.y;
        return this;
    }

    public Vec2f set(Vec3f xyz) {
        this.x = xyz.x;
        this.y = xyz.y;
        return this;
    }

    public Vec2f set(Vec4f xyzw) {
        this.x = xyzw.x;
        this.y = xyzw.y;
        return this;
    }


    public float len() {
        return (float) Math.hypot(x, y);
    }


    public Vec2f normalize() {
        float abs = (float) Math.hypot(x, y);
        x /= abs;
        y /= abs;
        return this;
    }

    public Vec2f add(Vec2f v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vec2f sub(Vec2f v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vec2f mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }


    public float dot(Vec2f v) {
        return this.x * v.x + this.y * v.y;
    }

    public float cross(Vec2f v) {
        return this.x * v.y - this.y * v.x;
    }


    public static Vec2f normalize(Vec2f v) {
        float abs = (float) Math.hypot(v.x, v.y);
        return new Vec2f(v.x / abs, v.y / abs);
    }


    public static Vec2f add(Vec2f a, Vec2f b) {
        return new Vec2f(a.x + b.x, a.y + b.y);
    }

    public static Vec2f sub(Vec2f a, Vec2f b) {
        return new Vec2f(a.x - b.x, a.y - b.y);
    }

    public static Vec2f mul(Vec2f v, float scalar) {
        return new Vec2f(v.x * scalar, v.y * scalar);
    }

    public static float dot(Vec2f a, Vec2f b) {
        return a.x * b.x + a.y * b.y;
    }

    public static float cross(Vec2f a, Vec2f b) {
        return a.x * b.y - a.y * b.x;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec2f)) return false;

        Vec2f other = (Vec2f) obj;
        return this.x == other.x
                && this.y == other.y;
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
}
