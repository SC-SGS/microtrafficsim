package microtrafficsim.osm.parser.relations.restriction;

import java.util.ArrayList;

import microtrafficsim.osm.parser.relations.RelationFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation.Restriction;
import microtrafficsim.osm.primitives.Primitive;
import microtrafficsim.osm.primitives.Relation;
import microtrafficsim.osm.primitives.RelationMember;


/**
 * Relation-Factory for the {@code RestrictionRelation}. Only parses/generates
 * general {@code restriction}s and ignores other (eg. {@code restriction:hgv} etc.)
 * 
 * @author Maximilian Luz
 */
public class RestrictionRelationFactory implements RelationFactory {
	
	/**
	 * Returns the type of the given restriction (see OpenStreetMap wiki or {@code
	 * Restriction} for these types).
	 * 
	 * @param r	the Relation to extract the type from.
	 * @return the Restriction-Type of the given Relation.
	 */
	private Restriction getRestrictionType(Relation r) {
		String restrictionVal = r.tags.get("restriction");
		if (restrictionVal == null) return null;
		
		switch (restrictionVal) {
		case "no_right_turn":
			return Restriction.NO_RIGHT_TURN;
			
		case "no_left_turn":
			return Restriction.NO_LEFT_TURN;
			
		case "no_u_turn":
			return Restriction.NO_U_TURN;
			
		case "no_straight_on":
			return Restriction.NO_STRAIGHT_ON;
			
		case "only_right_turn":
			return Restriction.ONLY_RIGHT_TURN;
			
		case "only_left_turn":
			return Restriction.ONLY_LEFT_TURN;
			
		case "only_straight_on":
			return Restriction.ONLY_STRAIGHT_ON;
			
		case "no_entry":
			return Restriction.NO_ENTRY;
			
		case "no_exit":
			return Restriction.NO_EXIT;
			
		default:
			return null;
		}
	}

	@Override
	public RestrictionRelation create(Relation r) {
		Restriction restriction = getRestrictionType(r);
		if (restriction == null) return null;
		
		ArrayList<Long> from = new ArrayList<>();
		ArrayList<Long> to = new ArrayList<>();
		ArrayList<Long> via = new ArrayList<>();
		
		for (RelationMember m : r.members) {
			switch (m.role) {
			case "from":
				from.add(m.ref);
				break;
				
			case "to":
				to.add(m.ref);
				break;
				
			case "via":
				via.add(m.ref);
				break;
				
			default:
				// ignore
			}
		}
		
		Primitive.Type viaType = Primitive.Type.NODE;
		if (via.size() > 1)
			viaType = Primitive.Type.WAY;
		
		return new RestrictionRelation(r.id, restriction, from, to, via, viaType);
	}
}
