package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;


/**
 * Simple area area, defined by a list of {@link Coordinate Coordinate}s.
 *
 * @author Maximilian Luz
 */
public class SimplePolygon implements ISimplePolygon {

    private Coordinate[] coordinates;

    public SimplePolygon(Coordinate[] coordinates) {
        if (coordinates.length < 3) throw new IllegalArgumentException();
        this.coordinates = coordinates;
    }

    @Override
    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    @Override
    public boolean contains(Coordinate p) {
        /*
         * The point-in-polygon test is calculated using a, for integer values modified,
         * version of the winding number algorithm described in 'A Winding Number and
         * Point-in-Polygon Algorithm' by David G. Alciatore, Dept. of Mechanical
         * Engineering, Colorado State University.
         * (https://www.engr.colostate.edu/~dga/dga/papers/point_in_polygon.pdf)
         */

        int windings = 0;   // actually the doubled number of windings

        double x1 = coordinates[coordinates.length - 1].lon - p.lon;
        double y1 = coordinates[coordinates.length - 1].lat - p.lat;
        for (Coordinate c : coordinates) {
            double x2 = c.lon - p.lon;
            double y2 = c.lat - p.lat;

            if (y1 * y2 < 0) {                                  // (1) --> (2) crosses the x-axis
                double r = x1 + (y1 * (x2 - x1)) / (y1 - y2);   // x-coordinate of intersection of (1) --> (2) and x-axis
                if (r > 0) {                                    // (1) --> (2) crosses positive x-axis
                    if (y1 < 0) windings += 2;
                    else        windings -= 2;
                }
            } else if (y1 == 0 && x1 > 0) {                     // (1) is on the positive x-axis
                if (y2 > 0) windings += 1;
                else        windings -= 1;
            } else if (y2 == 0 && x2 > 0) {                     // (2) is on the positive x-axis
                if (y1 < 0) windings += 1;
                else        windings -= 1;
            }

            x1 = x2;
            y1 = y2;
        }

        return windings != 0;
    }
}
