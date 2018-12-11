package javaMaps;

import java.util.concurrent.ConcurrentHashMap;

import map.MyMap;

public class ConcurrentMap<K, V> implements MyMap<K,V> {

	private ConcurrentHashMap<K, V> internalData;
	
	public ConcurrentMap(int cap, float load, int numThreads) {
		internalData = new ConcurrentHashMap<>(cap, load, numThreads);
	}
	
	public V put(K key, V value) {
		return internalData.put(key, value);
	}
	
	public V get(K key) {
		return internalData.get(key);
	}
	
	public boolean containsKey(K key) {
		return internalData.containsKey(key);
	}
}
