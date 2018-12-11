package javaMaps;


import java.util.Map;
import java.util.HashMap;
import map.MyMap;

public class CoarseGrainedMap<K, V> implements MyMap<K, V> {

	private Map<K, V> internalData;
	
	public CoarseGrainedMap(int cap) {
		internalData = new HashMap<>();
	}
	
	public synchronized V put(K key, V value) {
		return internalData.put(key, value);
	}
	
	public synchronized V get(K key) {
		return internalData.get(key);
	}
	
	public synchronized boolean containsKey(K key) {
		return internalData.containsKey(key);
	}
}
