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
import java.lang.reflect.Type;
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
     * Returns all the {@link Type}s contained by this registry
     * at a certain point of time. The registry can contains more
     * than one {@link Id} for a given type. Therefore the size of the
     * {@code Set} returned by {@link #ids()} is {@code >=} to the size
     * of the {@code Set} returned by this method.
     * <p>The returned {@code Set} can be link to the registry state. Any modification
     * to the content of the registry may have to be reported to this {@code Set}. You can
     * see it as a in sync with the registry. But it must be unmodifiable.</p>
     * <p>This intent of this method is to provide information on the content
     * of the {@code Registry}. It can be used for monitoring and supervision.</p>
     * <p>Warning: Due to the dynamic nature of the {@code Registry} the returned
     * {@code Set} must be immutable. And you must get, it use and discard it. Keeping
     * a long reference to it can introduce issues as memory leak. As it is a snapshot
     * of the {@code Registry} content it as only informational value.</p>
     *
     * @return all the types contained by this registry.
     */
    Set<Type> types();

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
     *
     * @param id  not {@code null} {@link Id} whose associated {@link Supplier} is to be returned.
     * @param <T> the matching type between the {@link Id} parameter and the returned {@link Supplier}
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
     * @param id  not {@code null} {@link Id} whose associated {@link Supplier}s are to be returned
     * @param <T> the matching type between the class parameter and the returned {@link Supplier}s
     * @return the list of {@link Supplier}s value to which the specified {@link Id} is mapped, or
     *         an empty list if this map contains no mapping for the given {@link Id}.
     * @throws NullPointerException if the given {@link Id} parameter is {@code null}.
     */
    <T> List<Supplier<T>> getAll(Id<T> id);

    /**
     * Returns the first {@link Supplier} to which the specified type is mapped
     * or {@code null} if the map contains no mapping for this type.
     * <p/>
     * <p>More formally, if this registry contains at least a mapping for a type
     * then it returns the first available mapping where for a given {@code TypeToken} {@code t} for all
     * ids in the registry if {@code id.type()} == {@code t.getType()}</p>
     * <p>This implementation is lest strict that the {@link #get(Id)} because it just validate
     * the type and forget the annotation.</p>
     *
     * @param type not {@code null} type whose associated {@link Supplier} is to be returned
     * @param <T>  the matching type between the class parameter and the returned {@link Supplier}
     * @return the value to which the specified type is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the given type parameter is {@code null}
     */
    @Nullable
    <T> Supplier<T> get(TypeToken<T> type);

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

    /**
     * Associates the specified supplier with the specified id in this registry.
     * If the map previously contained a mapping for the id, the new supplier is
     * added after the old one or an runtime exception is throw for duplicated entry,
     * This behaviour depends on the implementation.
     * <p/>
     * (A registry {@code r} is said to contain a mapping for a id {@code i} if
     * and only if {@link #contains(Id) r.contains(i)} would return {@code true}.)
     *
     * @param id       id with which the specified supplier is to be associated.
     * @param supplier supplier to be associated with the specified id
     * @return the registration handle for use by the caller to remove this
     *         registration action. It not intents to be shared by the caller.
     * @throws NullPointerException if the specified key or value is null
     *                              and this map does not permit null keys or values
     * @see Registration
     * @see #remove(Registration)
     */
    <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier);

    /**
     * Removes the supplier referenced by the specified
     * {@code Registration} object.
     *
     * @param registration A reference to the supplier to be released.
     */
    void remove(Registration<?> registration);

    /**
     * Bulk remove the suppliers referenced by the specified list of
     * {@code Registration} objects.
     *
     * @param registrations A list of references to the suppliers to be released.
     */
    void removeAll(Collection<? extends Registration<?>> registrations);

    /**
     * Registers a listener / watcher for suppliers life-cycle events (add / remove).
     * The registry will notify the listener when a supplier is added of removed
     * and its Id is matched by the given {@link Id} matcher.
     * <p><b>Warning:</b> To avoid memory leak watcher are stored in the registry
     * with weak reference. Therefore the caller must keep a strong reference to
     * the registration to avoid premature garbage collection of the watcher.</p>
     *
     * @param idMatcher the matcher that matches supplier ids the listener
     *                  should be notified of.
     * @param watcher   the watcher for suppliers whose associated ids are
     *                  matched by idMatcher
     * @param <T>       the supplied type
     * @return the registration that represent this action. This registration is
     *         the only way to remove this watcher from the registry. It is not intended
     *         to be shared by the caller
     */
    <T> Registration<T> addWatcher(IdMatcher<T> idMatcher, Watcher<T> watcher);

    /**
     * Removes the {@link Watcher} instance referenced by the specified
     * {@code Registration} object.
     *
     * @param watcherRegistration A reference to the watcher to be released.
     */
    void removeWatcher(Registration<?> watcherRegistration);

    /**
     * Bulk remove the {@link Watcher Watchers} referenced by the specified list of
     * {@code Registration Registration}.
     *
     * @param watcherRegistrations A list of references to the watchers to be released.
     */
    void removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations);

}
