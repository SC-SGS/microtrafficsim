package microtrafficsim.core.vis.mesh.utils;

import microtrafficsim.math.Vec2d;


/**
 * Utilities for triangles.
 *
 * @author Maximilian Luz
 */
public class Triangles {
    private Triangles() {}

    /**
     * Checks if point {@code p} is contained inside the triangle described by {@code a}, {@code b} and {@code c}.
     *
     * @param a the first point of the triangle.
     * @param b the first point of the triangle.
     * @param c the first point of the triangle.
     * @param p the point to test for inclusion.
     * @return {@code true} if {@code p} is contained in the triangle described by {@code a}, {@code b} and {@code c}.
     */
    public static boolean contains(Vec2d a, Vec2d b, Vec2d c, Vec2d p) {
        final double s1 = (c.x - b.x) * (p.y - b.y) - (c.y - b.y) * (p.x - b.x);
        final double s2 = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
        final double s3 = (a.x - c.x) * (p.y - c.y) - (a.y - c.y) * (p.x - c.x);
        return s1 >= 0.0 && s2 >= 0.0 && s3 >= 0.0;
    }
}
