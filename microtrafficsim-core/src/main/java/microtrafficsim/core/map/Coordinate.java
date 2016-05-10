package microtrafficsim.core.map;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Coordinate described by latitude longitude.
 *
 * 
 * @author Maximilian Luz
 */
public class Coordinate {
	public double lat, lon;


	public static Coordinate normalized(double lat, double lon) {
		return new Coordinate(((lat + 90) % 180) - 90, ((lon + 180) % 360) - 180);
	}

	public Coordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public Coordinate(Coordinate c) {
		this(c.lat, c.lon);
	}
	
	public Coordinate set(Coordinate c) {
		this.lat = c.lat;
		this.lon = c.lon;
		return this;
	}
	
	public Coordinate set(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		return this;
	}

	public Coordinate normalize() {
		lat = ((lat + 90) % 180) - 90;
		lon = ((lon + 180) % 360) - 180;
		return this;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Coordinate))
			return false;
		
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
