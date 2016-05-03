package microtrafficsim.core.map.polygon;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class Rect implements Polygon {

    public final float minLat;
    public final float maxLat;
    public final float minLon;
    public final float maxLon;

    public Rect(float minLat, float maxLat, float minLon, float maxLon) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    @Override
    public boolean contains(Coordinate c) {
        return minLat <= c.lat && c.lat <= maxLat
                && minLon <= c.lon && c.lon <= maxLon;
    }

    @Override
    public boolean contains(Mappable m) {
        Coordinate c = m.getCoordinate();
        return minLat <= c.lat && c.lat <= maxLat
                && minLon <= c.lon && c.lon <= maxLon;
    }
}
