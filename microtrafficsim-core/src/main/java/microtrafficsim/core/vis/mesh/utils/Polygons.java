package microtrafficsim.core.vis.mesh.utils;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.collections.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Polygon utilities.
 *
 * @author Maximilian Luz
 */
public class Polygons {
    private Polygons() {}

    /**
     * Calculates the area of the polygon described by the given contour.
     *
     * @param polygon the contour of the polygon for which the area should be calculated.
     * @return the area of the given polygon. This will be the negative area if the
     * order of the given points is clockwise, positive if counter-clockwise.
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

    /**
     * Clips the polygon described by the given contour to the given bounds, using the Sutherland-Hodgman algorithm.
     * <p> Note:
     *     This method requires {@code polygon[0].equals(polygon[polygon.length - 1])} and
     *     guarantees {@code result[0].equals(result[result.length - 1])}.
     * </p>
     *
     * @param bounds  the bounds to which the polygon should be clipped.
     * @param polygon the contour of the polygon which should be clipped.
     * @return the contour of the clipped polygon,
     */
    public static Coordinate[] clip(Bounds bounds, Coordinate[] polygon) {
        ArrayList<Coordinate> clipped = new ArrayList<>(polygon.length);

        // clip min-latitude
        {
            Coordinate a = polygon[polygon.length - 2];     // -2 due to start == end
            for (Coordinate b : polygon) {
                if (b.lat >= bounds.minlat) {
                    if (a.lat < bounds.minlat) {
                        Coordinate c = new Coordinate(
                                bounds.minlat,
                                a.lon + (b.lon - a.lon) * (bounds.minlat - a.lat) / (b.lat - a.lat)
                        );
                        addIfNotAtEnd(clipped, c);
                    }
                    addIfNotAtEnd(clipped, b);
                } else if (a.lat >= bounds.minlat) {
                    Coordinate c = new Coordinate(
                            bounds.minlat,
                            a.lon + (b.lon - a.lon) * (bounds.minlat - a.lat) / (b.lat - a.lat)
                    );
                    addIfNotAtEnd(clipped, c);
                }

                a = b;
            }
            if (clipped.size() <= 2) return null;
        }

        // clip max-latitude
        {
            ArrayList<Coordinate> input = new ArrayList<>(clipped);
            clipped.clear();

            Coordinate a = input.get(input.size() - 1);
            for (Coordinate b : input) {
                if (b.lat <= bounds.maxlat) {
                    if (a.lat > bounds.maxlat) {
                        Coordinate c = new Coordinate(
                                bounds.maxlat,
                                a.lon + (b.lon - a.lon) * (bounds.maxlat - a.lat) / (b.lat - a.lat)
                        );
                        addIfNotAtEnd(clipped, c);
                    }
                    addIfNotAtEnd(clipped, b);
                } else if (a.lat <= bounds.maxlat) {
                    Coordinate c = new Coordinate(
                            bounds.maxlat,
                            a.lon + (b.lon - a.lon) * (bounds.maxlat - a.lat) / (b.lat - a.lat)
                    );
                    addIfNotAtEnd(clipped, c);
                }

                a = b;
            }
            if (clipped.size() <= 2) return null;
        }

        // clip min-longitude
        {
            ArrayList<Coordinate> input = new ArrayList<>(clipped);
            clipped.clear();

            Coordinate a = input.get(input.size() - 1);
            for (Coordinate b : input) {
                if (b.lon >= bounds.minlon) {
                    if (a.lon < bounds.minlon) {
                        Coordinate c = new Coordinate(
                                a.lat + (b.lat - a.lat) * (bounds.minlon - a.lon) / (b.lon - a.lon),
                                bounds.minlon
                        );
                        addIfNotAtEnd(clipped, c);
                    }
                    addIfNotAtEnd(clipped, b);
                } else if (a.lon >= bounds.minlon) {
                    Coordinate c = new Coordinate(
                            a.lat + (b.lat - a.lat) * (bounds.minlon - a.lon) / (b.lon - a.lon),
                            bounds.minlon
                    );
                    addIfNotAtEnd(clipped, c);
                }

                a = b;
            }
            if (clipped.size() <= 2) return null;
        }

        // clip max-longitude
        {
            ArrayList<Coordinate> input = new ArrayList<>(clipped);
            clipped.clear();

            Coordinate a = input.get(input.size() - 1);
            for (Coordinate b : input) {
                if (b.lon <= bounds.maxlon) {
                    if (a.lon > bounds.maxlon) {
                        Coordinate c = new Coordinate(
                                a.lat + (b.lat - a.lat) * (bounds.maxlon - a.lon) / (b.lon - a.lon),
                                bounds.maxlon
                        );
                        addIfNotAtEnd(clipped, c);
                    }
                    addIfNotAtEnd(clipped, b);
                } else if (a.lon <= bounds.maxlon) {
                    Coordinate c = new Coordinate(
                            a.lat + (b.lat - a.lat) * (bounds.maxlon - a.lon) / (b.lon - a.lon),
                            bounds.maxlon
                    );
                    addIfNotAtEnd(clipped, c);
                }

                a = b;
            }
            if (clipped.size() <= 2) return null;
        }

        // ensure start == end
        if (!clipped.get(0).equals(clipped.get(clipped.size() - 1)))
            clipped.add(new Coordinate(clipped.get(0)));

        return clipped.toArray(new Coordinate[clipped.size()]);
    }

    private static void addIfNotAtEnd(ArrayList<Coordinate> dest, Coordinate c) {
        if (dest.isEmpty() || !dest.get(dest.size() - 1).equals(c))
            dest.add(c);
    }

    /**
     * Triangulates the given polygon.
     *
     * @param polygon the polygon to triangulate.
     * @return the triangulated polygon as sequence of indices pointing to the points in the original sequence.
     */
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

        int nv    = polygon.length;
        int count = 2 * nv;

        ArrayList<Integer> indices = new ArrayList<>();

        for (int v = nv - 1; nv > 2;) {
            if (0 >= (count--)) return null;

            int u          = v;
            if (nv <= u) u = 0;
            v              = u + 1;
            if (nv <= v) v = 0;
            int w          = v + 1;
            if (nv <= w) w = 0;

            if (triangulateSnip(polygon, u, v, w, nv, ccw)) {
                int a, b, c, s, t;

                a = ccw[u];
                b = ccw[v];
                c = ccw[w];

                indices.add(a);
                indices.add(b);
                indices.add(c);

                for (s = v, t = v + 1; t < nv; s++, t++)
                    ccw[s] = ccw[t];
                nv--;

                count = 2 * nv;
            }
        }

        return ArrayUtils.toArray(indices, new int[indices.size()]);
    }

    /**
     * Helper method for {@link Polygons#triangulate(Vec2d[])}
     */
    private static boolean triangulateSnip(Vec2d[] polygon, int u, int v, int w, int n, int[] ccw) {
        Vec2d a = polygon[ccw[u]];
        Vec2d b = polygon[ccw[v]];
        Vec2d c = polygon[ccw[w]];

        if (0.0000000001 > (((b.x - a.x) * (c.y - a.y)) - ((b.y - a.y) * (c.x - a.x))))
            return false;

        for (int i = 0; i < n; i++) {
            if (i == u || i == v || i == w) continue;
            if (Triangles.contains(a, b, c, polygon[ccw[i]])) return false;
        }

        return true;
    }
}
