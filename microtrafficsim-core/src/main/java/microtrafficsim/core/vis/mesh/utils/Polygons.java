package microtrafficsim.core.vis.mesh.utils;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.collections.ArrayUtils;

import java.util.ArrayList;


/**
 * Polygon utilities.
 *
 * @author Maximilian Luz
 */
public class Polygons {
    private Polygons() {}

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

                        if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                            clipped.add(c);
                    }

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(b))
                        clipped.add(b);
                } else if (a.lat >= bounds.minlat) {
                    Coordinate c = new Coordinate(
                            bounds.minlat,
                            a.lon + (b.lon - a.lon) * (bounds.minlat - a.lat) / (b.lat - a.lat)
                    );

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                        clipped.add(c);
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

                        if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                            clipped.add(c);
                    }

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(b))
                        clipped.add(b);
                } else if (a.lat <= bounds.maxlat) {
                    Coordinate c = new Coordinate(
                            bounds.maxlat,
                            a.lon + (b.lon - a.lon) * (bounds.maxlat - a.lat) / (b.lat - a.lat)
                    );

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                        clipped.add(c);
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

                        if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                            clipped.add(c);
                    }

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(b))
                        clipped.add(b);

                } else if (a.lon >= bounds.minlon) {
                    Coordinate c = new Coordinate(
                            a.lat + (b.lat - a.lat) * (bounds.minlon - a.lon) / (b.lon - a.lon),
                            bounds.minlon
                    );

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                        clipped.add(c);
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

                        if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                            clipped.add(c);
                    }

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(b))
                        clipped.add(b);

                } else if (a.lon <= bounds.maxlon) {
                    Coordinate c = new Coordinate(
                            a.lat + (b.lat - a.lat) * (bounds.maxlon - a.lon) / (b.lon - a.lon),
                            bounds.maxlon
                    );

                    if (clipped.isEmpty() || !clipped.get(clipped.size() - 1).equals(c))
                        clipped.add(c);
                }

                a = b;
            }
        }

        // ensure start == end
        if (!clipped.get(0).equals(clipped.get(clipped.size() - 1)))
            clipped.add(new Coordinate(clipped.get(0)));

        if (clipped.size() < 4) return null;

        return clipped.toArray(new Coordinate[clipped.size()]);
    }
}
