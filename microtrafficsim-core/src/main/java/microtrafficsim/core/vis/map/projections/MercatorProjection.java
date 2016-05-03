package microtrafficsim.core.vis.map.projections;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.hashing.FNVHashBuilder;


// probably not the standard mercator-projection
public class MercatorProjection implements Projection {

	private double scale;

	public MercatorProjection() {
		this(180);
	}

	public MercatorProjection(double scale) {
		this.scale = scale;
	}


	public Bounds getMaximumBounds() {
		return unproject(getProjectedMaximumBounds());
	}

	public Rect2d getProjectedMaximumBounds() {
		return new Rect2d(-scale, -scale, scale, scale);
	}

	@Override
	public Vec2d project(Coordinate c) {
		double x = scale * c.lon / 180.0;
		double y = scale * Math.log(Math.tan(Math.PI/4 + Math.toRadians(c.lat)/2)) / Math.PI;
		
		return new Vec2d(x, y);
	}

	@Override
	public Coordinate unproject(Vec2d v) {
		double lat = Math.toDegrees((2*Math.atan(Math.exp(Math.PI * v.y / scale)) - Math.PI/2));
		double lon = 180.0 * v.x / scale;
		
		return new Coordinate(lat, lon);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MercatorProjection))
			return false;

		MercatorProjection other = (MercatorProjection) obj;

		return this.scale == other.scale;
	}
	
	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(scale)
				.getHash();
	}
}
