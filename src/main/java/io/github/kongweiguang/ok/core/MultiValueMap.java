package io.github.kongweiguang.ok.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 多个值的map
 *
 * @author kongweiguang
 */
public class MultiValueMap<K, V> {

    private final Map<K, Set<V>> map = new HashMap<>();

    public Map<K, Set<V>> map() {
        return map;
    }

    public void put(K key, V value) {
        final Set<V> list = map.computeIfAbsent(key, k -> new HashSet<>());
        list.add(value);
    }

    public Set<V> get(K key) {
        return map.getOrDefault(key, new HashSet<>());
    }

    public Set<V> removeKey(K key) {
        return map.remove(key);
    }

    public boolean removeValue(K key, V value) {
        final Set<V> list = map.computeIfAbsent(key, k -> new HashSet<>());
        return list.remove(value);
    }


}
