package microtrafficsim.core.vis.mesh.utils;


import microtrafficsim.math.Vec2d;

public class Triangles {
    private Triangles() {}

    public static boolean contains(Vec2d a, Vec2d b, Vec2d c, Vec2d p) {
        final double s1 = (c.x - b.x) * (p.y - b.y) - (c.y - b.y) * (p.x - b.x);
        final double s2 = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
        final double s3 = (a.x - c.x) * (p.y - c.y) - (a.y - c.y) * (p.x - c.x);
        return s1 >= 0.0 && s2 >= 0.0 && s3 >= 0.0;
    }
}
