package microtrafficsim.math;

import java.util.Iterator;

/**
 * This class is able to distribute a number of elements on a given number of buckets.
 * 
 * @author Dominic Parga Cacheiro
 */
public class Distribution {
	/**
	 * <p>
	 * Calculates an iterator returning as much ints as buckets are given. This
	 * method is stable against round exceptions. This means, it is guaranteed, the
	 * sum of all returned ints is the same number as the number of elements
	 * given as parameter.
	 * </p>
	 * 
	 * @param elementCount
	 *            So many elements should be distributed
	 * @param bucketCount
	 *            So many ints will be returned
	 * 
	 * @return An {@link Iterator} returning ints about how many elements one
	 *         bucket contains.
	 */
	public static Iterator<Integer> uniformly(int elementCount, int bucketCount) {
		
		return new Iterator<Integer>() {

			private int rest = elementCount;
			private int i = 0;
			
			@Override
			public boolean hasNext() {
				return i < bucketCount;
			}

			@Override
			public Integer next() {
				int temp = rest / (bucketCount - (i++));
				rest = rest - temp;
				return temp;
			}
			
		};
	}
}