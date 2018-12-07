package bookMaps;

import java.util.List;
import java.util.ArrayList;
import map.MyMap;

public abstract class BaseHashMap<K, V> implements MyMap<K, V> {

	protected List<Item>[] table;
	protected int setSize;

	public BaseHashMap(int capacity) {
		setSize = 0;
		// @SuppressWarnings("unchecked")
		table = (List<Item>[]) new List[capacity];
		for (int i = 0; i < capacity; i++) {
			table[i] = new ArrayList<Item>();
		}
	}
	
	public abstract void acquire(K k);
	
	public abstract void release(K k);
	
	public abstract boolean policy();
	
	public abstract void resize();

	public boolean containsKey(K k) {
		acquire(k);
		try {
			int myBucket = k.hashCode() % table.length;
			
			// Iterate through bucket to find my key.
			for(Item item : table[myBucket]) {
				if(item.key == k) {
					return true;
				}
			}
			return false;
		} finally {
			release(k);
		}
	}
	
	public V get(K k) {
		V value = null;
		acquire(k);
		try {
			int myBucket = k.hashCode() % table.length;
			// Iterate through bucket to find my key.
			for(Item item : table[myBucket]) {
				if(item.key == k) {
					value = item.value;
				}
			}
		} finally {
			release(k);
		}
		return value;
	}

	public V put(K k, V v) {
		acquire(k);
		try {
			Item myItem = new Item(k, v);
			int myBucket = k.hashCode() % table.length;
			
			// Remove any pre-existing item with the same key.
			Item toRemove = null;
			for(Item item : table[myBucket]) {
				if(item.key == k) {
					toRemove = item;
					break;
				}
			}
			if(toRemove != null) {
				table[myBucket].remove(toRemove);
				setSize -= 1;
			}
						
			table[myBucket].add(myItem);
			setSize +=1;
		} finally {
			release(k);
		}
		if (policy())
			resize();
		return v;
	}
	
	protected class Item {
		K key;
		V value;
		
		public Item(K k, V v) {
			key = k;
			value = v;
		}
	}

}
