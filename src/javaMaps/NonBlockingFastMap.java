package javaMaps;
import map.MyMap;
import high_scale_lib.NonBlockingHashMap;


public class NonBlockingFastMap<K, V> implements MyMap<K, V>{

private NonBlockingHashMap<K, V> internalData;
	
	public NonBlockingFastMap(int cap) {
		internalData = new NonBlockingHashMap<>(cap);
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
