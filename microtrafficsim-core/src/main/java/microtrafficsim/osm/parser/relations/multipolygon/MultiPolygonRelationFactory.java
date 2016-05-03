package microtrafficsim.osm.parser.relations.multipolygon;

import java.util.ArrayList;

import microtrafficsim.osm.parser.relations.RelationFactory;
import microtrafficsim.osm.primitives.Primitive;
import microtrafficsim.osm.primitives.Relation;
import microtrafficsim.osm.primitives.RelationMember;


/**
 * Factory to create {@code MultiPolygonRelation}s from an OpenStreetMap {@code
 * Relation} primitive.
 * 
 * @author Maximilian Luz
 */
public class MultiPolygonRelationFactory implements RelationFactory {

	@Override
	public MultiPolygonRelation create(Relation r) {
		ArrayList<Long> outer = new ArrayList<>();
		ArrayList<Long> inner = new ArrayList<>();
		
		for (RelationMember m : r.members) {
			if (m.type != Primitive.Type.WAY)
				continue;
			
			if (m.role.equals("outer")) {
				outer.add(m.ref);
			} else if (m.role.equals("inner")) {
				inner.add(m.ref);
			}
		}
		
		return new MultiPolygonRelation(r.id, outer, inner);
	}
}
