package microtrafficsim.core.map.polygon;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;
import microtrafficsim.math.DistanceCalculator;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class Circle implements Polygon {

    private Coordinate center;
    private double radius;
    private DistanceCalculator distcalc;

    public Circle(Coordinate center, Coordinate borderCoord) {
        this.center = center;
        distcalc = new CoordinateDistanceCalculator();
        radius = distcalc.getDistance(center, borderCoord);
    }

    public Circle(Coordinate center, Coordinate borderCoord, DistanceCalculator distcalc) {
        this.center = center;
        this.distcalc = distcalc;
        radius = this.distcalc.getDistance(this.center, borderCoord);
    }

    @Override
    public boolean contains(Coordinate c) {
        return distcalc.getDistance(center, c) <= radius;
    }

    @Override
    public boolean contains(Mappable m) {
        return distcalc.getDistance(center, m.getCoordinate()) <= radius;
    }

    /**
     * TODO
     *
     * @author Dominic Parga Cacheiro
     */
    private class CoordinateDistanceCalculator implements DistanceCalculator {

        public double getDistance(Coordinate a, Coordinate b) {
            double deltaLat = 2 * (a.lat - b.lat);
            double deltaLon = a.lon - b.lon;
            return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
        }
    }
}
