/*
 * Copyright 2013 Romain Gilles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yaor.guice;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

/**
 * TODO comment
 * Date: 3/11/13
 * Time: 9:05 PM
 *
 * @author Romain Gilles
 */
public class CacheContainer<K, V> implements Container<K, V> {


    private final LoadingCache<K, List<V>> loadingCache;

    static <K, V> CacheContainer<K, V> newConcurrentContainer() {
        return new CacheContainer<>(CacheBuilder.newBuilder().build(new CacheLoader<K, List<V>>() {
            @Override
            public List<V> load(K key) {
                return new CopyOnWriteArrayList<>();
            }
        }));
    }

    static <K, V> CacheContainer<K, V> newNonConcurrentContainer() {
        return new CacheContainer<>(CacheBuilder.newBuilder().build(new CacheLoader<K, List<V>>() {
            @Override
            public List<V> load(K key) {
                return newArrayList();
            }
        }));
    }

    public CacheContainer(LoadingCache<K, List<V>> loadingCache) {
        this.loadingCache = loadingCache;
    }

    @Override
    public List<V> getAll(K key) {
        try {
            return unmodifiableList(loadingCache.get(key));
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Nullable
    @Override
    public V getFirst(K key) {
        return Iterables.getFirst(getAll(key), null);
    }

    @Override
    public boolean put(K key, V value) {
        try {
            List<V> valueList = loadingCache.get(key);
            return valueList.add(value);
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Override
    public boolean remove(K key, V value) {
        try {
            return loadingCache.get(key).remove(value);
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Override
    public Map<K, ? extends Collection<V>> asMap() {
        return loadingCache.asMap();
    }
}
