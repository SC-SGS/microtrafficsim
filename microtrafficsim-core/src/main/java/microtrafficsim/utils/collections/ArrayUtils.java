package microtrafficsim.utils.collections;

import java.util.ArrayList;
import java.util.List;


/**
 * Array utilities.
 * 
 * @author Maximilian Luz
 */
public class ArrayUtils {
	private ArrayUtils() {}
	
	/**
	 * Copy the given List to the specified Array at offset zero.
	 * If {@code to} is {@code null} or has not enough capacity to fully store all
	 * elements in the specified List, a new one will be created and returned.
	 * If {@code to} is able to contain all data in {@code from}, {@code to} will
	 * be returned.
	 * 
	 * @param from		the List to copy the data from.
	 * @param to		the array to copy the data to.
	 * @return an Array containing all elements of {@code from}.
	 */
	public static int[] toArray(List<Integer> from, int[] to) {
		if (to == null || to.length < from.size()) {
			to = new int[from.size()];
		}
		
		int i = 0;
		for (Integer element : from) {
			to[i++] = element;
		}
		
		return to;
	}
	
	/**
	 * Copy the given List to the specified Array at offset zero.
	 * If {@code to} is {@code null} or has not enough capacity to fully store all
	 * elements in the specified List, a new one will be created and returned.
	 * If {@code to} is able to contain all data in {@code from}, {@code to} will
	 * be returned.
	 * 
	 * @param from		the List to copy the data from.
	 * @param to		the array to copy the data to.
	 * @return an Array containing all elements of {@code from}.
	 */
	public static long[] toArray(List<Long> from, long[] to) {
        if (to == null || to.length < from.size()) {
			to = new long[from.size()];
		}
		
		int i = 0;
		for (Long element : from) {
			to[i++] = element;
		}
		
		return to;
	}
	
	
	/**
	 * Creates a new ArrayList from the given Array (by copying).
	 * 
	 * @param from	the Array to create the list from.
	 * @return a new ArrayList containing all elements in the given Array.
	 */
	public static ArrayList<Integer> toList(int[] from) {
		ArrayList<Integer> list = new ArrayList<>(from.length);
		
		for (int i : from)
			list.add(i);
		
		return list;
	}
	
	/**
	 * Creates a new ArrayList from the given Array (by copying).
	 * 
	 * @param from	the Array to create the list from.
	 * @return a new ArrayList containing all elements in the given Array.
	 */
	public static ArrayList<Long> toList(long[] from) {
		ArrayList<Long> list = new ArrayList<>(from.length);
		
		for (long l : from)
			list.add(l);
		
		return list;
	}
	
	
	/**
	 * Checks if {@code array} contains {@code} value.
	 * 
	 * @param array	the {@code int}-array in which {@code value} is looked for.
	 * @param value	the {@code int}-value to look for.
	 * @return {@code true} iff {@code array} contains {@code} value.
	 */
	public static boolean contains(int[] array, int value) {
		for (int l : array)
			if (l == value)
				return true;
		
		return false;
	}
	
	/**
	 * Checks if {@code array} contains {@code} value.
	 * 
	 * @param array	the {@code long}-array in which {@code value} is looked for.
	 * @param value	the {@code long}-value to look for.
	 * @return {@code true} iff {@code array} contains {@code} value.
	 */
	public static boolean contains(long[] array, long value) {
		for (long l : array)
			if (l == value)
				return true;
		
		return false;
	}
	
	
	/**
	 * Checks if the given arrays are disjoint, i.e. no value is contained in both
	 * arrays.
	 * 
	 * @param a	the first array.
	 * @param b the second array.
	 * @return {@code true} iff {@code a} and {@code b} are disjoint.
	 */
	public static boolean disjoint(int[] a, int[] b) {
		for (int x : a)
			for (int y : b)
				if (x == y)
					return false;
		
		return true;
	}
	
	/**
	 * Checks if the given arrays are disjoint, i.e. no value is contained in both
	 * arrays.
	 * 
	 * @param a	the first array.
	 * @param b the second array.
	 * @return {@code true} iff {@code a} and {@code b} are disjoint.
	 */
	public static boolean disjoint(long[] a, long[] b) {
		for (long x : a)
			for (long y : b)
				if (x == y)
					return false;
		
		return true;
	}
	
	
	/**
	 * Checks if specified arrays equal each other if one of them is reversed.
	 * 
	 * @param a	the first array,
	 * @param b	the second array,
	 * @return {@code true} if {@code a} equals the reversed {@code b}.
	 */
	public static boolean reverseEquals(int[] a, int[] b) {
		if (a.length != b.length) return false;
		
		for (int i = 0; i < a.length; i++)
			if (a[a.length - 1 - i] != b[i])
				return false;
		
		return true;
	}
	
	/**
	 * Checks if specified arrays equal each other if one of them is reversed.
	 * 
	 * @param a	the first array,
	 * @param b	the second array,
	 * @return {@code true} if {@code a} equals the reversed {@code b}.
	 */
	public static boolean reverseEquals(long[] a, long[] b) {
		if (a.length != b.length) return false;
		
		for (int i = 0; i < a.length; i++)
			if (a[a.length - 1 - i] != b[i])
				return false;
		
		return true;
	}
	
	
	/**
	 * Result-type for {@link ArrayUtils#longestCommonSubstring(long[], long[])}.
	 */
	public static class LongestCommonSubstring {
		public final int startA;
		public final int startB;
		public final int length;
		
		public LongestCommonSubstring(int startA, int startB, int length) {
			this.startA = startA;
			this.startB = startB;
			this.length = length;
		}
	}
	
	/**
	 * Computes the longest common substring of both arrays.
	 * 
	 * @param a	the first array.
	 * @param b	the second array.
	 * @return the position and length of the longest common substring as {@code
	 * LongestCommonSubstring}.
	 */
	public static LongestCommonSubstring longestCommonSubstring(long[] a, long[] b) {
		int[] curr = new int[b.length];			// current score
		int[] prev = new int[b.length];			// previous score
		
		int len = 0;							// length of the substring
		int endidxA = 0;						// end index in a of substring
		int endidxB = 0;						// end index in b of substring
		
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				int score;
				
				if (a[i] != b[j]) {
					score = 0;
				} else if (i == 0 || j == 0) {
					score = 1;
				} else {
					score = prev[j - 1] + 1;
				}
				
				curr[j] = score;
				
				if (score > len) {
					len = score;
					endidxA = i;
					endidxB = j;
				}
			}
			
			int[] swp = prev;
			prev = curr;
			curr = swp;
		}
		
		if (len <= 0) return null;
        return new LongestCommonSubstring(endidxA - len + 1, endidxB - len + 1, len);
	}
}
