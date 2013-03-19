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

package org.yar.guice;

import org.yar.Supplier;
import org.yar.Watcher;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 3/19/13
 * Time: 9:58 AM
 *
 * @author Romain Gilles
 */
public abstract class AbstractSupplierTargetWatcher<T> implements Watcher<Supplier<T>> {
    protected final Watcher<? super T> registryListener;

    public AbstractSupplierTargetWatcher(Watcher listener) {
        registryListener = listener;
    }

    @Nullable
    @Override
    public Supplier add(Supplier<T> element) {
        T trackedElement = element.get();
        if (registryListener.add(trackedElement) != null) {
            return track(element, trackedElement);
        } else {
            return null;
        }
    }

    protected abstract Supplier track(Supplier<T> element, T trackedElement);

    @Override
    public void remove(Supplier<T> element) {
        registryListener.remove(requireNonNull(removeTracked(element), "trackedElement"));
    }

    protected abstract T removeTracked(Supplier<T> element);
}
