package org.javabits.yar.guice;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.hamcrest.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Romain Gilles
 *         Date: 6/3/13
 *         Time: 10:45 AM
 */
public class GuiceIdTest {
    @Test
    public void testToStringOfTypeOnly() throws Exception {
        assertThat(GuiceId.of(MyInterface.class).toString(), containsString(MyInterface.class.getSimpleName()));
    }

    @Test
    public void testToStringOfTypeAndAnnotation() throws Exception {
        assertThat(GuiceId.of(Key.get(MyInterface.class, Names.named("test"))).toString(), containsString("test"));
    }
}
