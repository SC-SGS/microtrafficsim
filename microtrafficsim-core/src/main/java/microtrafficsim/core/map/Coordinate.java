package microtrafficsim.core.map;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Coordinate described by latitude (-90 to 90) and longitude (-180 to 180).
 *
 * @author Maximilian Luz
 */
public class Coordinate {
    public double lat, lon;

    /**
     * Constructs a new {@code Coordinate}.
     *
     * @param lat the latitude of the coordinate
     * @param lon the longitude of the coordinate
     */
    public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Copy-constructs a new {@code Coordinate}:
     *
     * @param c the coordinate to copy.
     */
    public Coordinate(Coordinate c) {
        this(c.lat, c.lon);
    }

    /**
     * Creates a normalized {@code Coordinate}. The coordinate is normalized to [-90, 90] (latitude) and [-180 to 180]
     * (longitude).
     *
     * @param lat the latitude of the coordinate.
     * @param lon the longitude of the coordinate.
     * @return the (normalized) coordinate.
     */
    public static Coordinate normalized(double lat, double lon) {
        return new Coordinate(((lat + 90) % 180) - 90, ((lon + 180) % 360) - 180);
    }

    /**
     * Sets this coordinate by copying the specified one.
     *
     * @param c the coordinate to copy.
     * @return this {@code Coordinate}.
     */
    public Coordinate set(Coordinate c) {
        this.lat = c.lat;
        this.lon = c.lon;
        return this;
    }

    /**
     * Sets this coordinate.
     *
     * @param lat the new latitude of the coordinate.
     * @param lon the new longitude of the coordinate.
     * @return this {@code Coordinate}.
     */
    public Coordinate set(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    /**
     * Normalizes this coordinate. The coordinate is normalized to [-90, 90] (latitude) and [-180 to 180] (longitude).
     *
     * @return this {@code Coordinate}.
     */
    public Coordinate normalize() {
        lat = ((lat + 90) % 180) - 90;
        lon = ((lon + 180) % 360) - 180;
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinate)) return false;

        Coordinate other = (Coordinate) obj;
        return this.lat == other.lat && this.lon == other.lon;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(lat)
                .add(lon)
                .getHash();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { " + lat + ", " + lon + "}";
    }
}
