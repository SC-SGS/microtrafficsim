package microtrafficsim.core.map;


/**
 * Describes the boundaries of a rectangular map-segment.
 * 
 * @author Maximilian Luz
 */
public class Bounds implements Cloneable {
	public double minlat, minlon, maxlat, maxlon;

	public Bounds(double minlat, double minlon, double maxlat, double maxlon) {
		this.minlat = minlat;
		this.minlon = minlon;
		this.maxlat = maxlat;
		this.maxlon = maxlon;
	}
	
	public Bounds(Coordinate min, Coordinate max) {
		this.minlat = min.lat;
		this.minlon = min.lon;
		this.maxlat = max.lat;
		this.maxlon = max.lon;
	}
	
	public Bounds(Bounds other) {
		this.minlat = other.minlat;
		this.minlon = other.minlon;
		this.maxlat = other.maxlat;
		this.maxlon = other.maxlon;
	}
	
	
	public Bounds set(double minlat, double minlon, double maxlat, double maxlon) {
		this.minlat = minlat;
		this.minlon = minlon;
		this.maxlat = maxlat;
		this.maxlon = maxlon;
		return this;
	}
	
	public Bounds set(Coordinate min, Coordinate max) {
		this.minlat = min.lat;
		this.minlon = min.lon;
		this.maxlat = max.lat;
		this.maxlon = max.lon;
		return this;
	}
	
	public Bounds set(Bounds other) {
		this.minlat = other.minlat;
		this.minlon = other.minlon;
		this.maxlat = other.maxlat;
		this.maxlon = other.maxlon;
		return this;
	}
	
	
	public Coordinate min() {
		return new Coordinate(minlat, minlon);
	}
	
	public Coordinate max() {
		return new Coordinate(maxlat, maxlon);
	}
	
	
	@Override
	public String toString() {
		return this.getClass().getName() + " {" + minlat + ", " + minlon + ", " + maxlat + ", " + maxlon + "}";
	}
}
