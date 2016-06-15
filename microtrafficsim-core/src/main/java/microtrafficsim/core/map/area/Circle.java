package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.DistanceCalculator;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * Circular area, defined radius and method of distance-calculation. The distance calculator
 * can be specified to define the shape of the circular area, by default the
 * {@link HaversineDistanceCalculator} is used, creating a circle in relation to real-world distance.
 * Note: the shape in the projected (i.e. visualized) form may vary, depending on the projection.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class Circle implements Area {

    private Coordinate center;
    private double radius;
    private DistanceCalculator distcalc;

    /**
     * Creates a new circular area with the specified center and border-coordinate.
     * This call is equivalent to {@link Circle#Circle(Coordinate, Coordinate, DistanceCalculator)
     * Circle(center, onBorder, HaversineDistanceCalculator::getDistance)}
     *
     * @param center    the center of the new area.
     * @param onBorder  a coordinate laying on the border of the area, specifying the distance to the center.
     */
    public Circle(Coordinate center, Coordinate onBorder) {
        this(center, onBorder, HaversineDistanceCalculator::getDistance);
    }

    /**
     * Creates a new circular area with the specified center, border-coordinate and distance calculator.
     * This call is equivalent to {@link Circle#Circle(Coordinate, double, DistanceCalculator)
     * Circle(center, distcalc.getDistance(center, onBorder), distcalc)}
     *
     * @param center    the center of the new area.
     * @param onBorder  a coordinate laying on the border of the area, specifying the distance to the center.
     * @param distcalc  the method used for distance calculation.
     */
    public Circle(Coordinate center, Coordinate onBorder, DistanceCalculator distcalc) {
        this(center, distcalc.getDistance(center, onBorder), distcalc);
    }

    /**
     * Creates a new circular area with the specified center and radius.
     * This call is equivalent to {@link Circle#Circle(Coordinate, double, DistanceCalculator)
     * Circle(center, radius, HaversineDistanceCalculator::getDistance)}
     *
     * @param center    the center of the new area.
     * @param radius    the radius of the new area.
     */
    public Circle(Coordinate center, double radius) {
        this(center, radius, HaversineDistanceCalculator::getDistance);
    }

    /**
     * Creates a new circular area with the specified center, radius and DistanceCalculator.
     *
     * @param center    the center of the new area.
     * @param radius    the radius of the new area.
     * @param distcalc  the method used for distance calculation.
     */
    public Circle(Coordinate center, double radius, DistanceCalculator distcalc) {
        this.center = center;
        this.radius = radius;
        this.distcalc = distcalc;
    }

    @Override
    public boolean contains(Coordinate c) {
        return distcalc.getDistance(center, c) <= radius;
    }
}
