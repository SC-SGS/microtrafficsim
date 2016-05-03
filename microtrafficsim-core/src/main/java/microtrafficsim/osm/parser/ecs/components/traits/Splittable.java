package microtrafficsim.osm.parser.ecs.components.traits;

import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;


/**
 * Interface to provide an extra way of splitting {@code Components}.
 * 
 * @author Maximilian Luz
 */
public interface Splittable {
	
	/**
	 * Split this {@code Component} onto the specified {@code WayEntities}. Note
	 * that {@code Components} are first cloned, and thus already exist on the
	 * splits. This method is intended as a means of post-processing these splits.
	 * 
	 * @param dataset		the {@code DataSet} to which all entities belong.
	 * @param splits		the already splitted {@code WayEntities}.
	 * @param splitpoints	the indices on which to split this {@code Component}.
	 */
	void split(DataSet dataset, WayEntity[] splits, int[] splitpoints);
}
