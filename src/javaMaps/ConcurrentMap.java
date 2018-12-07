package javaMaps;

import java.util.concurrent.ConcurrentHashMap;

import map.MyMap;

public class ConcurrentMap<K, V> implements MyMap<K,V> {

	private ConcurrentHashMap<K, V> internalData;
	
	public ConcurrentMap() {
		internalData = new ConcurrentHashMap<>();
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
