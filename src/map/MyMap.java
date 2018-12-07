package map;

public interface MyMap<K, V> {
	
	public boolean containsKey(K key);
	
	public V get(K key);
	
	public V put(K key, V val);
		
}
