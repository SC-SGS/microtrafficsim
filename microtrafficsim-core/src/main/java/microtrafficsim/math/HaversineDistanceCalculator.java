package microtrafficsim.math;

import microtrafficsim.core.map.Coordinate;


/**
 * Distance calculator implementation using the Haversine Formula.
 * 
 * @author Maximilian Luz
 */
public class HaversineDistanceCalculator {
	private HaversineDistanceCalculator() {}
	
	public static final double EARTH_MEAN_RADIUS = 6371000f;					// in meter
	public static final double EARTH_MEAN_DIAMETER = EARTH_MEAN_RADIUS * 2.0f;	// in meter
	
	
	/**
	 * Calculate the distance using the Haversine-Formula. Coordinates are
	 * expected to be in degree.
	 * 
	 * @param a	the first coordinate.
	 * @param b	the second coordinate.
	 * @return the distance between {@code a} and {@code b} in meter.
	 */
	public static double getDistance(Coordinate a, Coordinate b) {
		double deltalat = Math.toRadians(b.lat - a.lat);
		double deltalon = Math.toRadians(b.lon - a.lon);
		
		double alatr = Math.toRadians(a.lat);
		double blatr = Math.toRadians(b.lat);
		
		double slat = Math.sin(deltalat / 2);
		double slon = Math.sin(deltalon / 2);
		
		return EARTH_MEAN_DIAMETER
				* Math.asin(Math.sqrt(slat*slat + Math.cos(alatr)
				* Math.cos(blatr)
				* slon*slon));
	}
}
