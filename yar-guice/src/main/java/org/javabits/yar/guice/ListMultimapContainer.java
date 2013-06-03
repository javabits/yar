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

package org.javabits.yar.guice;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;

/**
 * TODO comment
 * Date: 2/22/13
 * Time: 11:52 AM
 *
 * @author Romain Gilles
 */
class ListMultimapContainer<K, V> implements Container<K, V> {
    private final ListMultimap<K, V> delegate;

    ListMultimapContainer(ListMultimap<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<V> getAll(K key) {
        List<V> pairs = delegate.get(key);
        synchronized (delegate) {
            return ImmutableList.copyOf(pairs);
        }
    }

    @Override
    @Nullable
    public V getFirst(K key) {
        List<V> pairs = delegate.get(key);
        synchronized (delegate) {
            if (!pairs.isEmpty()) {
                return pairs.get(0);
            }
        }
        return null;
    }

    @Override
    public boolean put(K key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public boolean remove(K key, V value) {
        return delegate.remove(key, value);
    }

    @Override
    public Map<K, ? extends Collection<V>> asMap() {
        synchronized (delegate) {
            return delegate.asMap();
        }
    }

    static <K, V> ListMultimapContainer<K, V> newSynchronizedContainer() {
        return new ListMultimapContainer<>(synchronizedListMultimap(ArrayListMultimap.<K, V>create()));
    }

    static <K, V> ListMultimapContainer<K, V> newLockFreeContainer() {
        return new ListMultimapContainer<>(ArrayListMultimap.<K, V>create());
    }

    @Override
    public void invalidate(K key) {
        delegate.removeAll(key);
    }

    @Override
    public void invalidateAll(Iterable<K> keys) {
        for (K key : keys) {
            invalidate(key
            );
        }

    }

    @Override
    public void addKeyListener(KeyListener<K> keyListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeKeyListener(KeyListener<K> keyListener) {
        throw new UnsupportedOperationException();
    }
}
