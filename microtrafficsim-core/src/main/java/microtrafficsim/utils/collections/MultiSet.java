package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.Iterator;


/**
 * Interface for MultiSets used to store elements in a Set-like data-structure,
 * where every entry is associated with a count (the number of occurrences of
 * this entry).
 * 
 * @param <E>	the type of elements stored in this MultiSet.
 * @author Maximilian Luz
 */
public interface MultiSet<E> extends Collection<E> {
	
	/**
	 * Entry of a MultiSet to access both value and count.
	 * 
	 * @param <T>	the type of elements stored in the MultiSet.
	 */
	interface Entry<T> {
		
		/**
		 * Returns the element stored in this entry.
		 * @return the element in this entry.
		 */
		T getElement();
		
		/**
		 * Returns the count of this entry's value.
		 * @return the count of this entry.
		 */
		int getCount();
	}
	
	/**
	 * Removes one occurrence of the given object from this MultiSet. If there is
	 * only one occurence, removes this object completely.
	 * 
	 * @param obj	the Object to remove.
	 * @return true if the MultiSet has been changed by this operation, thus
	 * returns true if {@code obj} has been in this MultiSet prior to this call.
	 */
	@Override
	boolean remove(Object obj);
	
	/**
	 * Removes one occurence of all given Objects.
	 * 
	 * @param c	the collection of Objects to remove.
	 * @return true if the MultiSet has been changed by this operation, thus
	 * returns true if any element of the given collection has been in this
	 * MultiSet prior to this call.
	 */
	@Override
	boolean removeAll(Collection<?> c);
	
	/**
	 * Removes all occurrences of the given object from this MultiSet, ignores the
	 * count of this entry.
	 * 
	 * @param obj	the Object to remove.
	 * @return true if the MultiSet has been changed by this operation, thus
	 * returns true if {@code obj} has been in this collection prior to this call.
	 */
	boolean removeCompletely(Object obj);
	
	/**
	 * Removes all occurences of all given Objects.
	 * 
	 * @param c	the collection of Objects to remove.
	 * @return true if the MultiSet has been changed by this operation, thus
	 * returns true if any element of the given collection has been in this
	 * MultiSet prior to this call.
	 */
	boolean removeAllCompletely(Collection<?> c);
	
	
	/**
	 * Removes all elements that are not contained in the given Collection from
	 * this MultiSet.
	 * 
	 * @param c collection containing elements to be retained in this collection.
	 * @return true if this MultiSet has been changed by this operation.
	 */
	@Override
	boolean retainAll(Collection<?> c);
	
	
	/**
	 * Returns the number of distinct Elements in this MultiSet, multiple
	 * occurrences of the same Element are ignored.
	 * 
	 * @return the number of distinct Elements.
	 */
	@Override
	int size();
	
	/**
	 * Returns the overall number of Elements on this MultiSet, multiple
	 * occurrences of the same Element are counted.
	 * 
	 * @return the overall number of Elements.
	 */
	int count();
	
	/**
	 * Returns the number of occurrences of the given Object in this MultiSet.
	 * 
	 * @param obj	the Object to get the number of occurrences for.
	 * @return the number of occurences of the given Object.
	 */
	int count(Object obj);
	
	
	/**
	 * Converts this MultiSet to an Array and returns this. Multiple occurrences are
	 * ignored.
	 * 
	 * @return an array containing all of the elements in this MultiSet.
	 */
	@Override
	Object[] toArray();
	
	/**
	 * Converts this MultiSet to an Array and returns this. Multiple occurrences are
	 * ignored.
	 * 
	 * @param <T>	the runtime type of the array to contain the collection.
	 * @param a		the array into which the elements of this collection are to be
	 * 				stored, if it is big enough; otherwise, a new array of the same runtime
	 * 				type is allocated for this purpose.
	 * @return an array containing all of the elements in this MultiSet.
	 */
	@Override
	<T> T[] toArray(T[] a);
	
	
	/**
	 * Returns an Iterator over all elements contained in this MultiSet, ignoring
	 * multiple occurrences.
	 * 
	 * @return an {@code Iterator} over the elements in this MultiSet.
	 */
	@Override
	Iterator<E> iterator();
	
	/**
	 * Returns an Iterator over all Entries (element-count pairs) contained in this
	 * MultiSet, ignoring multiple occurrences.
	 * 
	 * @return an {@code Iterator} over the Entries in this MultiSet.
	 */
	Iterator<Entry<E>> entryIterator();
	
	
	@Override
	boolean equals(Object other);
}
