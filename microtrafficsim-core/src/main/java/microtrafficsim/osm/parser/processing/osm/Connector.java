package microtrafficsim.osm.parser.processing.osm;

import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Street-Connector indicating a possibility to drive from one {@code WayEntity}
 * via a {@code NodeEntity} to another.
 * 
 * @author Maximilian Luz
 */
public class Connector {
	
	public final NodeEntity via;
	public final WayEntity from;
	public final WayEntity to;

	public Connector(NodeEntity via, WayEntity from, WayEntity to) {
		this.via = via;
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Connector))
			return false;

		Connector other = (Connector) obj;

		return this.via == other.via && this.from == other.from && this.to == other.to;
	}

	@Override
	public int hashCode() {
		return new FNVHashBuilder()
				.add(via)
				.add(from)
				.add(to)
				.getHash();
	}

	@Override
	public String toString() {
		return this.getClass().toString() + " { " + from.id + " >-- (" + via.id + ") --> " + to.id + " } ";
	}
}