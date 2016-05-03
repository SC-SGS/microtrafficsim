package microtrafficsim.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * Abstract base-class for map-based multi-sets.
 * Implements all functions of {@code MultiSet}.
 * 
 * @param <E>	the type of elements stored in this MultiSet.
 * @author Maximilian Luz
 */
public class AbstractMapBasedMultiSet<E> implements MultiSet<E> {
	
	private Map<E, Count> map;
	
	
	/**
	 * Creates a new {@code AbstractMapBasedMultiSet} with the specified map as
	 * backing.
	 * 
	 * @param map	the Map used as backing.
	 */
	protected AbstractMapBasedMultiSet(Map<E, Count> map) {
		this.map = map;
	}
	
	/**
	 * Creates a new {@code AbstractMapBasedMultiSet} with the specified map as
	 * backing filled with the elements of the given collection.
	 * 
	 * @param map	the Map used as backing.
	 * @param c		the elements with which to initialize this MultiSet.
	 */
	protected AbstractMapBasedMultiSet(Map<E, Count> map, Collection<? extends E> c) {
		this.map = map;
		
		if (c instanceof AbstractMapBasedMultiSet<?>) {
			@SuppressWarnings("unchecked")	// is actually checked via Collection<? extends E>
			AbstractMapBasedMultiSet<? extends E> mbs = (AbstractMapBasedMultiSet<? extends E>) c;
			for (E e : mbs)
				map.put(e, new Count(mbs.count(e)));
			
		} else {
			addAll(c);
		}
	}


	@Override
	public boolean add(E e) {
		Count count = map.get(e);
		
		if (count == null) {
			count = new Count(1);
			map.put(e, count);
		} else {
			count.count++;
		}
		
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		c.forEach(this::add);
		return true;
	}

	
	@Override
	public boolean remove(Object obj) {
		Count count = map.get(obj);
		
		if (count == null)
			return false;
		
		count.count--;
		if (count.count <= 0) {
			map.remove(obj);
		}
			
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		
		for (Object o : c) {
			if (remove(o)) {
				changed = true;
			}
		}
		
		return changed;
	}

	@Override
	public boolean removeCompletely(Object obj) {
		return map.keySet().remove(obj);
	}

	@Override
	public boolean removeAllCompletely(Collection<?> c) {
		return map.keySet().removeAll(c);
	}
	
	
	@Override
	public void clear() {
		map.clear();
	}


	@Override
	public boolean contains(Object obj) {
		return map.containsKey(obj);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return map.keySet().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}


	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}
	
	@Override
	public Iterator<MultiSet.Entry<E>> entryIterator() {
		return new EntryIterator<>(map.entrySet().iterator());
	}
	
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return map.keySet().retainAll(c);
	}


	@Override
	public int size() {
		return map.size();
	}

	@Override
	public int count() {
		int count = 0;
		
		for (Map.Entry<E, Count> entry : map.entrySet()) {
			count += entry.getValue().count;
		}
		
		return count;
	}

	@Override
	public int count(Object obj) {
		Count c = map.get(obj);
		return c != null ? c.count : 0;
	}
	

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return map.keySet().toArray(array);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof AbstractMapBasedMultiSet<?>)
				&& ((AbstractMapBasedMultiSet<?>) obj).map.equals(this.map);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	
	/**
	 * Simple mutable {@code int}-wrapper.
	 */
	protected static class Count {
		public int count;
		
		public Count(int count) {
			this.count = count;
		}
		
		@Override
		public boolean equals(Object other) {
			return (other instanceof Count)
					&& ((Count) other).count == this.count;
		}
	}
	
	
	/**
	 * Iterator for Map-based MultiSets.
	 * 
	 * @param <T>	the type of elements stored in the MultiSet.
	 * @author Maximilian Luz
	 */
	protected static class EntryIterator<T> implements Iterator<MultiSet.Entry<T>> {
		
		/**
	 	* Implementation for the {@code MultiSet.Entry}.
	 	* 
	 	* @param <T>	the type of elements stored in the MultiSet.
	 	*/
		private static class EntryImpl<T> implements MultiSet.Entry<T> {
			private T value;
			private int count;
			
			public EntryImpl(T value, int count) {
				this.value = value;
				this.count = count;
			}
		
			@Override
			public T getElement() {
				return value;
			}
		
			@Override
			public int getCount() {
				return count;
			}
		}
	
		
		private Iterator<Map.Entry<T, Count>> mapIterator;
		
		/**
		 * Creates a new EntryIterator based on the given Map-iterator.
		 * 
		 * @param mapIterator	the entry-iterator of the backing map.
		 */
		public EntryIterator(Iterator<Map.Entry<T, Count>> mapIterator) {
			this.mapIterator = mapIterator;
		}

		@Override
		public boolean hasNext() {
			return mapIterator.hasNext();
		}

		@Override
		public MultiSet.Entry<T> next() {
			Map.Entry<T, Count> entry = mapIterator.next();
			return new EntryImpl<>(entry.getKey(), entry.getValue().count);
		}
	}
}
