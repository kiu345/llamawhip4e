package com.github.kiu345.eclipse.eclipseai.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.e4.core.di.annotations.Creatable;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class ExpiringCache<K, V> implements Map<K, V> {
    private static class CacheEntry<V> {
        final V value;
        final long expiry;

        CacheEntry(V value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiry;
        }
    }

    private static class CMEntry<K, V> implements Map.Entry<K, V> {
        final K key;
        volatile V val;

        CMEntry(K key, V val) {
            this.key = key;
            this.val = val;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return val;
        }

        public final int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }

        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?, ?>) o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = val) || v.equals(u)));
        }
    }

    private final ConcurrentMap<K, CacheEntry<V>> map = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    public ExpiringCache() {
        long ttl = 5; // default
        this.ttlMillis = TimeUnit.SECONDS.toMillis(ttl);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ExpiringCache-Cleaner");
            t.setDaemon(true);
            return t;
        });
    }

    public V put(K key, V value) {
        map.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis));
        startCleanerIfNeeded();
        return value;
    }

    @Override
    public V get(Object key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null || entry.isExpired()) {
            map.remove(key);
            stopCleanerIfEmpty();
            return null;
        }
        return entry.value;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        return map.entrySet().stream().anyMatch(e -> value.equals(e.getValue()));
    }

    @Override
    public V remove(Object key) {
        ExpiringCache.CacheEntry<V> e = map.remove(key);
        stopCleanerIfEmpty();
        return e.value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == null) {
            return;
        }
        m.entrySet().stream().forEach(e -> put(e.getKey(), e.getValue()));
    }

    @Override
    public void clear() {
        map.clear();
        stopCleanerIfEmpty();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.entrySet().stream().map(e -> e.getValue().value).toList();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet().stream().map(e -> new CMEntry<K,V>(e.getKey(), e.getValue().value)).collect(Collectors.toSet());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void startCleanerIfNeeded() {
        if (running.compareAndSet(false, true)) {
            scheduler.scheduleAtFixedRate(
                    this::evictExpired,
                    ttlMillis, ttlMillis, TimeUnit.MILLISECONDS
            );
        }
    }

    private void stopCleanerIfEmpty() {
        if (map.isEmpty() && running.compareAndSet(true, false)) {
            // Scheduler bleibt aktiv, damit er später wieder starten kann
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        for (Map.Entry<K, CacheEntry<V>> e : map.entrySet()) {
            if (e.getValue().expiry <= now) {
                map.remove(e.getKey());
            }
        }
        stopCleanerIfEmpty();
    }
}
