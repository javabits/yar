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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;
import static org.javabits.yar.guice.KeyEvent.newKeyEvent;

/**
 * This class is responsible to maintain a multi-map of {@link Type}s
 * associated to values. It's implementation is based on Guava {@code LoadingCache}.
 * {@link KeyListener}{@literal <Type>} can be added. They will be
 * triggered on {@code CacheLoader.load(Type)} for addition, and on
 * {@link #invalidate(java.lang.reflect.Type)}, {@link #invalidateAll(Iterable)}
 * for removal.
 *
 * @param <V> the type of the multi-values associated to a {@link Type}
 *
 * @author Romain Gilles
 */
class CacheContainer<V> implements Container<Type, V> {


    private final LoadingCache<Type, List<V>> loadingCache;
    private final Collection<KeyListener<Type>> keyListeners;

    static <V> CacheContainer<V> newConcurrentContainer() {
        return newConcurrentContainer(new CopyOnWriteArrayList<KeyListener<Type>>());
    }

    private static <V> CacheContainer<V> newConcurrentContainer(final Collection<KeyListener<Type>> typeListeners) {
        return new CacheContainer<>(CacheBuilder.newBuilder().build(new CacheLoader<Type, List<V>>() {
            @Override
            public List<V> load(Type key) {
                for (KeyListener<Type> typeListener : typeListeners) {
                    typeListener.keyAdded(newKeyEvent(key));
                }
                return new CopyOnWriteArrayList<>();
            }
        }), typeListeners);
    }


    static <V> CacheContainer<V> newNonConcurrentContainer() {
        return new CacheContainer<>();
    }


    private CacheContainer() {
        final CopyOnWriteArrayList<KeyListener<Type>> keyListeners = new CopyOnWriteArrayList<>();
        this.keyListeners = keyListeners;
        this.loadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Type, List<V>>() {
            @Override
            public List<V> load(Type key) {
                for (KeyListener<Type> keyListener : keyListeners) {
                    keyListener.keyAdded(newKeyEvent(key));
                }
                return new CopyOnWriteArrayList<>();
            }
        });
    }

    private CacheContainer(LoadingCache<Type, List<V>> loadingCache, Collection<KeyListener<Type>> keyListeners) {
        this.loadingCache = loadingCache;
        this.keyListeners = keyListeners;
    }

    @Override
    public List<V> getAll(Type key) {
        try {
            return unmodifiableList(loadingCache.get(key));
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Nullable
    @Override
    public V getFirst(Type key) {
        return Iterables.getFirst(getAll(key), null);
    }

    @Override
    public boolean put(Type key, V value) {
        try {
            List<V> valueList = loadingCache.get(key);
            return valueList.add(value);
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Override
    public boolean remove(Type key, V value) {
        try {
            return loadingCache.get(key).remove(value);
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    @Override
    public void invalidate(Type key) {
        loadingCache.invalidate(key);
        fireKeyRemoved(key);

    }

    private void fireKeyRemoved(Type key) {
        for (KeyListener<Type> keyListener : keyListeners) {
            keyListener.keyRemoved(newKeyEvent(key));
        }
    }

    public void invalidateAll(Iterable<Type> keys) {
        loadingCache.invalidateAll(keys);
        for (Type key : keys) {
            fireKeyRemoved(key);
        }
    }

    @Override
    public Map<Type, ? extends Collection<V>> asMap() {
        return loadingCache.asMap();
    }

    @Override
    public void addKeyListener(KeyListener<Type> keyListener) {
        keyListeners.add(keyListener);
    }

    @Override
    public void removeKeyListener(KeyListener<Type> keyListener) {
        keyListeners.add(keyListener);
    }
}
