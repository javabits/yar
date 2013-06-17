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

package org.javabits.yar;


import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * It represent a storage to share capabilities with other component.
 * The capabilities are represented by Guava {@code Supplier}.
 *
 * @author Romain Gilles
 * @since 1.0
 */
@ThreadSafe
public interface Registry {

    TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    //5 minutes
    long DEFAULT_TIMEOUT = 1000 * 60 * 5L;

    /**
     * Returns all the {@link Id}s contained by this registry
     * at a certain point of time.
     * <p>The returned {@code Set} is not link to the registry. Any modification
     * to the content of the registry are not reported to this {@code Set}. You can
     * see it as a snapshot.</p>
     * <p>This intent of this method is to provide information on the content
     * of the {@code Registry}. It can be used for monitoring and supervision.</p>
     * <p>Warning: Due to the dynamic nature of the {@code Registry} the returned
     * {@code Set} must be immutable. And you must get, it use and discard it. Keeping
     * a long reference to it can introduce issues as memory leak. As it is a snapshot
     * of the {@code Registry} content it as only informational value.</p>
     *
     * @return all the ids contained by this registry.
     */
    Set<Id<?>> ids();

    /**
     * Returns the first {@link Supplier} to which the specified type is mapped
     * or {@code null} if the map contains no mapping for this type.
     * <p/>
     * <p>More formally, if this registry contains at least a mapping for a type
     * then it returns the first available mapping where for a given type {@code t} for all
     * ids in the registry returns the first {@link Supplier} where {@code id.type()} == {@code t}</p>
     *
     * @param type not {@code null} type whose associated {@link Supplier} is to be returned
     * @param <T>  the matching type between the class parameter and the returned {@link Supplier}
     * @return the value to which the specified type is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the given type parameter is {@code null}
     */
    @Nullable
    <T> Supplier<T> get(Class<T> type);

    /**
     * Returns all {@link Supplier}s to which the specified type is mapped
     * or an empty list if the map contains no mapping for this type.
     * <p/>
     * <p>More formally, if this registry contains a mapping for a type
     * then it returns all available mapping where for a given type {@code t}
     * ids {@code id} in the registry returns {@link Supplier}s
     * where {@code id.type()} == {@code t}</p>
     *
     * @param type not {@code null} type whose associated {@link Supplier}s are to be returned
     * @param <T>  the matching type between the class parameter and the returned {@link Supplier}s
     * @return the list of {@link Supplier}s value to which the specified type is mapped, or
     *         an empty list if this map contains no mapping for the given type.
     * @throws NullPointerException if the given type parameter is {@code null}.
     */
    <T> List<Supplier<T>> getAll(Class<T> type);

    /**
     * Returns the first {@link Supplier} to which the specified id is mapped
     * or {@code null} if the map contains no mapping for this id.
     * <p/>
     * <p>More formally, if this registry contains at least a mapping for an id
     * then it returns the first available mapping where for a given identifier {@code id} for all
     * ids {@code other} in the registry returns the first {@link Supplier} where {@code id.equals(other)}</p>
     * <p>This method is a strict comparison regarding the {@link #get(Class)}
     * and {@link #get(com.google.common.reflect.TypeToken)} which are more weak.</p>
     * @param id not {@code null} {@link Id} whose associated {@link Supplier} is to be returned.
     * @param <T>  the matching type between the {@link Id} parameter and the returned {@link Supplier}
     * @return the value to which the specified {@link Id} is mapped, or
     *         {@code null} if this map contains no mapping for the given {@link Id}.
     * @throws NullPointerException if the given {@link Id} parameter is {@code null}.
     */
    @Nullable
    <T> Supplier<T> get(Id<T> id);

    /**
     * Returns all {@link Supplier}s to which the specified {@link Id} is mapped
     * or an empty list if the map contains no mapping for this type.
     * <p/>
     * <p>More formally, if this registry contains a mapping for a {@link Id}
     * then it returns all available mapping where for a given {@link Id} id
     * ids {@code otherId} in the registry {@code id.equals(otherId)}</p>
     * <p>This method is a strict comparison regarding the {@link #getAll(Class)}
     * and {@link #getAll(com.google.common.reflect.TypeToken)} which are more weak.</p>
     *
     * @param id not {@code null} {@link Id} whose associated {@link Supplier}s are to be returned
     * @param <T>  the matching type between the class parameter and the returned {@link Supplier}s
     * @return the list of {@link Supplier}s value to which the specified {@link Id} is mapped, or
     *         an empty list if this map contains no mapping for the given {@link Id}.
     * @throws NullPointerException if the given {@link Id} parameter is {@code null}.
     */
    <T> List<Supplier<T>> getAll(Id<T> id);

    /**
     * Returns all {@link Supplier}s to which the specified {@code TypeToken} is mapped
     * or an empty list if the map contains no mapping for this type.
     * <p/>
     * <p>More formally, if this registry contains a mapping for a {@code TypeToken}
     * then it returns all available mapping where for a given {@code TypeToken} {@code t},
     * ids ({@code otherId}) in the registry match: {@code t.getType() == otherId.type()}</p>
     * <p>This method is not strict comparison regarding the {@link #getAll(Id)}
     * as it just validate the type and not the annotation.</p>
     * <p>This version of the getAll(...) method provide more type safety than the {@link #get(Class)}.</p>
     *
     * @param type not {@code null} {@code TypeToken} whose associated {@link Supplier}s are to be returned.
     * @param <T>  the matching type between the type parameter and the returned {@link Supplier}s
     * @return the list of {@link Supplier}s value to which the specified {@link Id} is mapped, or
     *         an empty list if this map contains no mapping for the given {@link Id}.
     * @throws NullPointerException if the given {@code TypeToken} parameter is {@code null}.
     */
    <T> List<Supplier<T>> getAll(TypeToken<T> type);

    <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier);

    void remove(Registration<?> registration);

    void removeAll(Collection<? extends Registration<?>> registrations);

    <T> Registration<T> addWatcher(IdMatcher<T> watchedKey, Watcher<T> watcher);

    void removeWatcher(Registration<?> watcherRegistration);

    void removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations);

}
