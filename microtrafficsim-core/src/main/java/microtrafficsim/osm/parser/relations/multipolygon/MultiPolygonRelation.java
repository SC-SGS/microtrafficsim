package microtrafficsim.osm.parser.relations.multipolygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import microtrafficsim.osm.parser.relations.RelationBase;


/**
 * Represents the OpenStreetMap {@code relation} element of type {@code
 * multipolygon}.
 * 
 * @author Maximilian Luz
 */
public class MultiPolygonRelation extends RelationBase {
	
	public List<Long> outer;
	public List<Long> inner;

	/**
	 * Creates a new MultiPolygonRelation with the specified references to inner
	 * and outer ways.
	 * 
	 * @param id	the ID of this relation.
	 * @param outer	a list of outer ways.
	 * @param inner a list of inner ways.
	 */
	public MultiPolygonRelation(long id, List<Long> outer, List<Long> inner) {
		super(id, "multipolygon");
		this.outer = outer;
		this.inner = inner;
	}

	@Override
	public Collection<Long> getRequiredNodes() {
		return new HashSet<>();
	}

	@Override
	public Collection<Long> getRequiredWays() {
		HashSet<Long> required = new HashSet<>(inner);
		required.addAll(outer);
		return required;
	}

	@Override
	public Class<? extends RelationBase> getType() {
		return MultiPolygonRelation.class;
	}
	
	@Override
	public MultiPolygonRelation clone() {
		return new MultiPolygonRelation(
				this.id,
				new ArrayList<>(this.outer),
				new ArrayList<>(this.inner));
	}
}
