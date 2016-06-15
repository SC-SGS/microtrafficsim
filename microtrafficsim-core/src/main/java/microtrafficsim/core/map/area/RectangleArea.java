package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;


/**
 * Rectangular, axis-aligned area, thus a simple area containing
 * four coordinates.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class RectangleArea implements ISimplePolygon {

    public final double minlat;
    public final double minlon;
    public final double maxlat;
    public final double maxlon;

    /**
     * Creates a rectangular axis-aligned area using the specified axis bounds.
     *
     * @param minlat    the minimum latitude
     * @param minlon    the minimum longitude
     * @param maxlat    the maximum latitude
     * @param maxlon    the maximum longitude
     */
    public RectangleArea(double minlat, double minlon, double maxlat, double maxlon) {
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
    }

    @Override
    public boolean contains(Coordinate c) {
        return minlat <= c.lat && c.lat <= maxlat
                && minlon <= c.lon && c.lon <= maxlon;
    }

    @Override
    public Coordinate[] getCoordinates() {
        return new Coordinate[] {
                new Coordinate(minlat, minlon),
                new Coordinate(minlat, maxlon),
                new Coordinate(maxlat, maxlon),
                new Coordinate(maxlat, minlon),
        };
    }
}
