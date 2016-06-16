package microtrafficsim.core.vis.mesh.utils;

import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.collections.ArrayUtils;

import java.util.ArrayList;


public class Polygons {
    private Polygons() {}

    /**
     * Calculates the area of the polygon described by the given contour.
     *
     * @param polygon   the contour of the polygon for which the area should be calculated.
     * @return          the area of the given polygon. This will be the negative area if the
     *                  order of the given points is clockwise, positive if counter-clockwise.
     */
    public static double area(Vec2d[] polygon) {
        double area = 0.0;

        Vec2d p = polygon[polygon.length - 1];
        for (Vec2d q : polygon) {
            area += p.x * q.y - q.x * p.y;
            p = q;
        }

        return area;
    }

    public static int[] triangulate(Vec2d[] polygon) {
        if (polygon.length < 3) throw new IllegalArgumentException();

        // currently adapted from http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
        // TODO: replace with triangle-strip generator

        int[] ccw = new int[polygon.length];
        if (area(polygon) > 0) {
            for (int i = 0; i < polygon.length; i++)
                ccw[i] = i;
        } else {
            for (int i = 0; i < polygon.length; i++)
                ccw[i] = polygon.length - 1 - i;
        }

        int nv = polygon.length;
        int count = 2*nv;

        ArrayList<Integer> indices = new ArrayList<>();

        for(int m=0, v=nv-1; nv>2; ) {
            if (0 >= (count--)) return null;

            int u = v  ; if (nv <= u) u = 0;
            v = u+1; if (nv <= v) v = 0;
            int w = v+1; if (nv <= w) w = 0;

            if (triangulateSnip(polygon,u,v,w,nv,ccw)) {
                int a,b,c,s,t;

                a = ccw[u]; b = ccw[v]; c = ccw[w];

                indices.add(a);
                indices.add(b);
                indices.add(c);

                m++;

                for(s=v,t=v+1;t<nv;s++,t++) ccw[s] = ccw[t]; nv--;

                count = 2*nv;
            }
        }

        return ArrayUtils.toArray(indices, new int[indices.size()]);
    }

    private static boolean triangulateSnip(Vec2d[] polygon, int u, int v, int w, int n, int[] ccw) {
        Vec2d a = polygon[ccw[u]];
        Vec2d b = polygon[ccw[v]];
        Vec2d c = polygon[ccw[w]];

        if (0.0000000001 > (((b.x-a.x)*(c.y-a.y)) - ((b.y-a.y)*(c.x-a.x))))
            return false;

        for (int i = 0; i < n; i++) {
            if(i == u || i == v || i == w)
                continue;
            if (Triangles.contains(a, b, c, polygon[ccw[i]]))
                return false;
        }

        return true;

    }
}
