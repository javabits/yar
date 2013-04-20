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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.name.Names;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.yar.guice.Matchers.*;

/**
 * TODO comment
 * Date: 3/18/13
 *
 * @author Romain Gilles
 */
public class MatchersTest {
    @Test
    public void testGetTypeLiteralOfMatcherOfKeyOfString() throws Exception {
        TypeLiteral<String> stringTypeLiteral = getTargetTypeLiteral(new AbstractMatcher<Key<String>>() {
            @Override
            public boolean matches(Key<String> item) {
                return false;
            }
        });
        TypeLiteral<String> expectedTypeLiteral = new TypeLiteral<String>() {
        };

        assertThat(stringTypeLiteral, is(not(nullValue())));
        assertThat(stringTypeLiteral, is(expectedTypeLiteral));
    }

    @Test
    public void testGetTypeLiteralOfKeyMatcherOfString() throws Exception {
        TypeLiteral<String> stringTypeLiteral = getTargetTypeLiteral(newKeyMatcher(String.class));
        TypeLiteral<String> expectedTypeLiteral = new TypeLiteral<String>() {
        };

        assertThat(stringTypeLiteral, is(not(nullValue())));
        assertThat(stringTypeLiteral, is(expectedTypeLiteral));
    }


    @Test
    public void testNewKeyMatcher_Key_true() {
        assertThat(newKeyMatcher(Key.get(String.class)).matches(Key.get(String.class)), is(true));
    }

    @Test
    public void testNewKeyMatcher_Key_false() {
        assertThat(newKeyMatcher(Key.get(String.class)).matches(Key.get(String.class, Names.named("toto"))), is(false));
    }

    @Test
    public void testNewKeyMatcher_Class_true() {
        assertThat(newKeyMatcher(String.class).matches(Key.get(String.class)), is(true));
    }

    @Test
    public void testNewKeyMatcher_Class_false() {
        assertThat(newKeyMatcher(String.class).matches(Key.get(String.class, Names.named("toto"))), is(false));
    }

    @Test
    public void testNewKeyTypeMatcher_Key() {
        assertThat(newKeyTypeMatcher(Key.get(String.class)).matches(Key.get(String.class)), is(true));
    }

    @Test
    public void testNewKeyTypeMatcher_Key_Named() {
        assertThat(newKeyTypeMatcher(Key.get(String.class)).matches(Key.get(String.class, Names.named("toto"))), is(true));
    }

    @Test
    public void testNewKeyTypeMatcher_Class() {
        assertThat(newKeyTypeMatcher(String.class).matches(Key.get(String.class)), is(true));
    }

    @Test
    public void testNewKeyTypeMatcher_Class_Named() {
        assertThat(newKeyTypeMatcher(String.class).matches(Key.get(String.class, Names.named("toto"))), is(true));
    }

}
