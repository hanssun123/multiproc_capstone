package bookMaps;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bookMaps.BaseHashMap.Item;

import java.util.List;
import java.util.ArrayList;

public class StripedHashMap<K, V> extends BaseHashMap<K, V> {
	final Lock[] locks;

	public StripedHashMap(int capacity) {
		super(capacity);
		locks = new ReentrantLock[capacity];
		for (int j = 0; j < locks.length; j++) {
			locks[j] = new ReentrantLock();
		}
	}

	public final void acquire(K k) {
		locks[k.hashCode() % locks.length].lock();
	}

	public void release(K k) {
		locks[k.hashCode() % locks.length].unlock();
	}

	public boolean policy() {
		return setSize / table.length > 4;
	}

	public void resize() {
		int oldCapacity = table.length;
		for (Lock lock : locks) {
			lock.lock();
		}
		try {
			if (oldCapacity != table.length) {
				return; // someone beat us to it
			}
			int newCapacity = 2 * oldCapacity;
			List<Item>[] oldTable = table;
			table = (List<Item>[]) new List[newCapacity];
			for (int i = 0; i < newCapacity; i++)
				table[i] = new ArrayList<Item>();
			for (List<Item> bucket : oldTable) {
				for (Item x : bucket) {
					table[x.hashCode() % table.length].add(x);
				}
			}
		} finally {
			for (Lock lock : locks) {
				lock.unlock();
			}
		}
	}
}