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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.javabits.yar.guice.CacheContainer.KeyConversionStrategies.NO_TYPE_ERASURE;
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
 * @author Romain Gilles
 */
class CacheContainer<V> implements Container<Type, V> {


    private final ConcurrentMap<Type, List<V>> loadingCache;
    private final Collection<KeyListener<Type>> keyListeners;
    private final Function<Type, Type> keyConversionStrategy;

    enum KeyConversionStrategies implements Function<Type, Type> {
        NO_TYPE_ERASURE() {
            @Nullable
            @Override
            public Type apply(@Nullable Type type) {
                return type;
            }
        },
        TYPE_ERASURE() {
            @Nullable
            @Override
            public Type apply(@Nullable Type type) {
                return Reflections.getRawType(type);
            }
        }
    }

    static <V> CacheContainer<V> newConcurrentContainer() {
        return newConcurrentContainer(new CopyOnWriteArrayList<>(), NO_TYPE_ERASURE);
    }

    private static <V> CacheContainer<V> newConcurrentContainer(final Collection<KeyListener<Type>> typeListeners, Function<Type, Type> keyConversionStrategy) {
        return new CacheContainer<>(new ConcurrentHashMap<>(), typeListeners, keyConversionStrategy);
    }

    static <V> CacheContainer<V> newNonConcurrentContainer(Function<Type, Type> keyConversionStrategy) {
        return new CacheContainer<>(keyConversionStrategy);
    }


    private CacheContainer(Function<Type, Type> keyConversionStrategy) {
        this.keyListeners = new CopyOnWriteArrayList<>();
        this.loadingCache = new ConcurrentHashMap<>();
        this.keyConversionStrategy = keyConversionStrategy;
    }

    private CacheContainer(ConcurrentMap<Type, List<V>> loadingCache, Collection<KeyListener<Type>> keyListeners, Function<Type, Type> keyConversionStrategy) {
        this.loadingCache = loadingCache;
        this.keyListeners = keyListeners;
        this.keyConversionStrategy = keyConversionStrategy;
    }

    @Override
    public List<V> getAll(Type key) {
        return unmodifiableList(getAllInternal(key));
    }

    private List<V> getAllInternal(Type key) {
        key = keyConversionStrategy.apply(key);
        return loadingCache.computeIfAbsent(key, k -> {
            for (KeyListener<Type> keyListener : keyListeners) {
                keyListener.keyAdded(newKeyEvent(k));
            }
            return new CopyOnWriteArrayList<>();
        });
    }

    @Nullable
    @Override
    public V getFirst(Type key) {
        return Iterables.getFirst(getAllInternal(key), null);
    }

    @Override
    public boolean put(Type key, V value) {
        List<V> valueList = getAllInternal(key);
        return valueList.add(value);
    }

    @Override
    public boolean remove(Type key, V value) {
        return getAllInternal(key).remove(value);
    }

    @Override
    public void invalidate(Type key) {
        key = keyConversionStrategy.apply(key);
        loadingCache.remove(key);
        fireKeyRemoved(key);
    }

    private void fireKeyRemoved(Type key) {
        for (KeyListener<Type> keyListener : keyListeners) {
            keyListener.keyRemoved(newKeyEvent(key));
        }
    }

    @Override
    public Map<Type, ? extends Collection<V>> asMap() {
        return unmodifiableMap(loadingCache);
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
