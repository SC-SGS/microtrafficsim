package microtrafficsim.osm.parser.relations;

import microtrafficsim.osm.primitives.Relation;


/**
 * Factory to create a parsed/abstracted Relation from an OpenStreetMap Relation
 * primitive.
 * 
 * @author Maximilian Luz
 */
public interface RelationFactory {
	
	/**
	 * Create and return a RelationBase from the given Relation primitive.
	 * 
	 * @param r	the Relation primitive from which the RelationBase should be
	 * 			created.
	 * @return the created RelationBase.
	 */
	RelationBase create(Relation r);
}
