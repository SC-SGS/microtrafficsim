package microtrafficsim.math;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * An axis-aligned (two-dimensional) {@code double}-based rectangle.
 *
 * @author Maximilian Luz
 */
public class Rect2d {
    public double xmin, ymin, xmax, ymax;

    /**
     * Constructs a new rectangle with the given properties.
     *
     * @param xmin the minimum {@code x}-axis value contained in the rectangle.
     * @param ymin the minimum {@code y}-axis value contained in the rectangle.
     * @param xmax the maximum {@code x}-axis value contained in the rectangle.
     * @param ymax the maximum {@code y}-axis value contained in the rectangle.
     */
    public Rect2d(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    /**
     * Constructs a new rectangle with the given parameters.
     *
     * @param min the minimum axis values contained in this rectangle.
     * @param max the maximum axis values contained in this rectangle.
     */
    public Rect2d(Vec2d min, Vec2d max) {
        this.xmin = min.x;
        this.ymin = min.y;
        this.xmax = max.x;
        this.ymax = max.y;
    }

    /**
     * Constructs a new rectangle by copying the specified one.
     *
     * @param other the rectangle from which the new rectangle should be copy-constructed.
     */
    public Rect2d(Rect2d other) {
        this.xmin = other.xmin;
        this.ymin = other.ymin;
        this.xmax = other.xmax;
        this.ymax = other.ymax;
    }

    /**
     * Sets this rectangle according to the specified parameters.
     *
     * @param xmin the minimum {@code x}-axis value contained in the rectangle.
     * @param ymin the minimum {@code y}-axis value contained in the rectangle.
     * @param xmax the maximum {@code x}-axis value contained in the rectangle.
     * @param ymax the maximum {@code y}-axis value contained in the rectangle.
     * @return this rectangle.
     */
    public Rect2d set(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        return this;
    }

    /**
     * Sets this rectangle according to the specified parameters.
     *
     * @param min the minimum axis values contained in this rectangle.
     * @param max the maximum axis values contained in this rectangle.
     * @return this rectangle.
     */
    public Rect2d set(Vec2d min, Vec2d max) {
        this.xmin = min.x;
        this.ymin = min.y;
        this.xmax = max.x;
        this.ymax = max.y;
        return this;
    }

    /**
     * Sets this rectangle by copying the specified one.
     *
     * @param other the rectangle from which the new rectangle should be copy-constructed.
     * @return this rectangle.
     */
    public Rect2d set(Rect2d other) {
        this.xmin = other.xmin;
        this.ymin = other.ymin;
        this.xmax = other.xmax;
        this.ymax = other.ymax;
        return this;
    }

    /**
     * Returns the minimum axis values of this rectangle.
     *
     * @return the minimum axis values.
     */
    public Vec2d min() {
        return new Vec2d(xmin, ymin);
    }

    /**
     * Returns the maximum axis values of this rectangle.
     *
     * @return the maximum axis values.
     */
    public Vec2d max() {
        return new Vec2d(xmax, ymax);
    }


    /**
     * Projects the given point from the given source rectangle to the given target rectangle.
     *
     * @param from the rectangle indicating the source coordinate system.
     * @param to   the rectangle indicating the target coordinate system.
     * @param p    the point to project.
     * @return     {@code p} projected from {@code from} to {@code to}.
     */
    public static Vec2d project(Rect2d from, Rect2d to, Vec2d p) {
        return new Vec2d(
            to.xmin + (p.x - from.xmin) / (from.xmax - from.xmin) * (to.xmax - to.xmin),
            to.ymin + (p.y - from.ymin) / (from.ymax - from.ymin) * (to.ymax - to.ymin)
        );
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rect2d)) return false;

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
