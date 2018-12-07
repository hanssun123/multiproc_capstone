package bookMaps;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;

public class CoarseHashMap<K, V> extends BaseHashMap<K, V> {
	final Lock lock;

	public CoarseHashMap(int capacity) {
		super(capacity);
		lock = new ReentrantLock();
	}

	public final void acquire(K k) {
		lock.lock();
	}

	public void release(K k) {
		lock.unlock();
	}

	public boolean policy() {
		return setSize / table.length > 4;
	}

	public void resize() {
		int oldCapacity = table.length;
		lock.lock();
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
			lock.unlock();
		}
	}
}
