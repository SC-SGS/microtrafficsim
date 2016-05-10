package microtrafficsim.osm.parser.ecs.components.traits;

import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.processing.osm.Ways.MergePoint;
import microtrafficsim.osm.parser.processing.osm.Ways.WayLayout;


/**
 * Interface to indicate that a this {@code Component} can be merged with
 * another {@code Component}.
 * 
 * @param <T>	the type of the {@code Component} with which this {@code
 * 				Component} can be merged. This should normally be the type of the
 * 				{@code Component} on which this interface is applied.
 * @author Maximilian Luz
 */
public interface Mergeable<T extends Component> {
	
	/**
	 * Checks if this {@code Component} and {@code other} can be merged without
	 * reversing one of them.
	 * 
	 * @param dataset	the {@code DataSet} to which the {@code Components} and
	 * 					{@code Entities} belong.
	 * @param layout	the {@code WayLayout} on the merge-point of the underlying
	 * 					{@code WayEntities}.
	 * @param node		the {@code NodeEntity} on the merge-point.
	 * @param other		the {@code Component} to test this {@code Component} for
	 * 					merge-possibilities.
	 * @return {@code true} if this and {@code other} can be merged without
	 * reversing one of them.
	 */
	boolean forwardMergeable(DataSet dataset, WayLayout layout, NodeEntity node, T other);
	
	/**
	 * Checks if this {@code Component} and {@code other} can be merged when one of
	 * them is reversed.
	 * 
	 * @param dataset	the {@code DataSet} to which the {@code Components} and
	 * 					{@code Entities} belong.
	 * @param layout	the {@code WayLayout} on the merge-point of the underlying
	 * 					{@code WayEntities}.
	 * @param node		the {@code NodeEntity} on the merge-point.
	 * @param other		the {@code Component} to test this {@code Component} for
	 * 					merge-possibilities.
	 * @return {@code true} if this and {@code other} can be merged without
	 * reversing one of them.
	 */
	boolean reverseMergeable(DataSet dataset, WayLayout layout, NodeEntity node, T other);
	
	/**
	 * Merges this {@code Component} with {@code other}.
	 * 
	 * @param dataset		the {@code DataSet} to which the {@code Components} and
	 * 						{@code Entities} belong.
	 * @param mergepoint	the {@code MergePoint} on which the {@code Components}
	 * 						should be merged.
	 * @param other			the {@code Component} with which this should be merged.
	 */
	void merge(DataSet dataset, MergePoint mergepoint, T other);
}
