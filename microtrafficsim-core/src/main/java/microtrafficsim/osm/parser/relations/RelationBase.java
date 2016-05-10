package microtrafficsim.osm.parser.relations;

import java.util.Collection;
import java.util.HashSet;


/**
 * Base-class for abstracted/parsed OpenStreetMap {@code relation} elements.
 * 
 * @author Maximilian Luz
 */
public abstract class RelationBase implements Cloneable {
	
	public long id;
	public final String osmType;
	
	public RelationBase(long id, String type) {
		this.id = id;
		this.osmType = type;
	}

	
	/**
	 * Returns a list of references to all Node-Elements this Relation
	 * requires.  This method should <em>never</em> return {@code null}.
	 * Relations depending on certain Nodes should overwrite this method to
	 * forward transitive dependencies to the Parser-Framework.
	 * 
	 * @return all references to Nodes this Relation requires.
	 */
	public Collection<Long> getRequiredNodes() {
		return new HashSet<>();
	}
	
	/**
	 * Returns a list of references to all Way-Elements this Relation requires.
	 * This method should <em>never</em> return {@code null}. Relations
	 * depending on certain Ways should overwrite this method to forward
	 * transitive dependencies to the Parser-Framework.
	 * 
	 * @return all references to Ways this Relation requires.
	 */
	public Collection<Long> getRequiredWays() {
		return new HashSet<>();
	}
	
	/**
	 * Returns the type of this Relation as Class-object. The returned value
	 * might differ from {@link #getClass()}.
	 * 
	 * @return the type of this Relation.
	 */
	public abstract Class<? extends RelationBase> getType();
	
	/**
	 * Clones this RelationBase instance.
	 * 
	 * @return a clone of this.
	 */
	public abstract RelationBase clone();
}
