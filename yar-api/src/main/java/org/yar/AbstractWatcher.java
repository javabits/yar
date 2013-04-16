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

package org.yar;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * TODO comment
 * Date: 3/19/13
 *
 * @author Romain Gilles
 */
public abstract class AbstractWatcher<T> implements Watcher<T> {
    @Nullable
    @Override
    final public Supplier<T> add(Supplier<T> element) {
        Supplier<T> trackedElement = doAdd(element);
        if (trackedElement != null) {
            track(trackedElement);
        }
        return trackedElement;
    }

    @Nullable
    protected abstract Supplier<T> doAdd(Supplier<T> element);

    protected abstract void track(Supplier<T> element);

    @Override
    final public void remove(Supplier<T> element) {
        if (isTracked(element)) {
            doRemove(element);
        }
    }

    protected abstract boolean isTracked(Supplier<T> element);

    protected abstract void doRemove(Supplier<T> element);
}
