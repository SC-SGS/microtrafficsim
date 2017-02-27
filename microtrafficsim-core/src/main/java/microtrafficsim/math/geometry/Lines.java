package microtrafficsim.math.geometry;


import microtrafficsim.math.Vec2d;

public class Lines {
    private Lines() {}

    /**
     * Checks if the line segments (p, q) described by the given vertex-pairs intersect.
     *
     * @param pa the first vertex of segment p
     * @param pb the second vertex of segment p
     * @param qa the first vertex of segment q
     * @param qb the second vertex of segment q
     * @return {@code true} if the specified line segments intersect (inside the segment boundaries) and are not
     * (partially) coincidental.
     */
    public static boolean segmentIntersectsNonCoincidental(Vec2d pa, Vec2d pb, Vec2d qa, Vec2d qb) {
        double pdx = pa.x - pb.x;
        double pdy = pa.y - pb.y;

        double qdx = qa.x - qb.x;
        double qdy = qa.y - qb.y;

        double d = pdx * qdy - pdy * qdx;
        if (d == 0.0)
            return false;

        // test if intersection is in segment
        double abdx = qa.x - pa.x;
        double abdy = qa.y - pa.y;

        double s = pdx * abdy - abdx * pdy;
        if (Math.signum(d) * s <= 0 || Math.signum(d) * s >= Math.abs(d))
            return false;

        double t = abdy * qdx - abdx * qdy;
        if (Math.signum(d) * t <= 0 || Math.signum(d) * t >= Math.abs(d))
            return false;

        return true;
    }

}
