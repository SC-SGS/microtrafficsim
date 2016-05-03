package microtrafficsim.osm.parser.relations.restriction;

import java.util.ArrayList;
import java.util.List;

import microtrafficsim.osm.parser.relations.RelationBase;
import microtrafficsim.osm.primitives.Primitive;


/**
 * Restriction relation, representing the OpenStreetMap {@code relation} with
 * {@code type=restriction}.
 * 
 * @author Maximilian Luz
 */
public class RestrictionRelation extends RelationBase {
	
	/**
	 * Type of a restriction
	 */
	public enum Restriction {
		NO_RIGHT_TURN,
		NO_LEFT_TURN,
		NO_U_TURN,
		NO_STRAIGHT_ON,
		ONLY_RIGHT_TURN,
		ONLY_LEFT_TURN,
		ONLY_STRAIGHT_ON,
		NO_ENTRY,
		NO_EXIT;
	
		/**
		 * Returns {@code true} if this restriction is a {@code NO}-type restriction
		 * (i.e. {@code NO_XXXX}). A restriction is either {@code NO}-type or {@code
		 * ONLY}-type.
		 * 
		 * @return {@code true} if this restriction is a {@code NO}-type restriction.
		 */
		public boolean isNoType() {
			return this == NO_RIGHT_TURN
					|| this == NO_LEFT_TURN
					|| this == NO_U_TURN
					|| this == NO_STRAIGHT_ON
					|| this == NO_ENTRY
					|| this == NO_EXIT;
		}
	
		/**
		 * Returns {@code true} if this restriction is a {@code ONLY}-type restriction
		 * (i.e. {@code ONLY_XXXX}). A restriction is either {@code NO}-type or {@code
		 * ONLY}-type.
		 * 
		 * @return {@code true} if this restriction is a {@code ONLY}-type restriction.
		 */
		public boolean isOnlyType() {
			return this == ONLY_RIGHT_TURN
					|| this == ONLY_LEFT_TURN
					|| this == Restriction.ONLY_STRAIGHT_ON;
		}
	}
	
	
	public Restriction restriction;
	public List<Long> from;
	public List<Long> to;
	public List<Long> via;
	public Primitive.Type viaType;
	
	public RestrictionRelation(long id, Restriction restriction, List<Long> from,
			List<Long> to, List<Long> via, Primitive.Type viaType) {
		
		super(id, "restriction");
		this.restriction = restriction;
		this.from = from;
		this.to = to;
		this.via = via;
		this.viaType = viaType;
	}

	@Override
	public Class<? extends RelationBase> getType() {
		return RestrictionRelation.class;
	}
	
	@Override
	public RestrictionRelation clone() {
		return new RestrictionRelation(
				this.id,
				this.restriction,
				new ArrayList<>(this.from),
				new ArrayList<>(this.to),
				new ArrayList<>(this.via),
				this.viaType);
	}
}
