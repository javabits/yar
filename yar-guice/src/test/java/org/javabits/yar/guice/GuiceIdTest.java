package org.javabits.yar.guice;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.junit.Test;

import javax.inject.Named;
import javax.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.javabits.yar.Ids.newId;

/**
 * @author Romain Gilles
 *         Date: 6/3/13
 *         Time: 10:45 AM
 */
public class GuiceIdTest {
    @Test
    public void testToStringOfTypeAndAnnotation() throws Exception {
        assertThat(GuiceId.of(Key.get(MyInterface.class, Names.named("test"))).toString(), containsString("test"));
    }

    @Test
    public void testEqualIdVsKey() throws Exception {
        Id<MyInterface> keyBasedId = GuiceId.of(Key.get(MyInterface.class, Names.named("test")));
        assertThat(keyBasedId, is(newId(MyInterface.class, Names.named("test"))));
    }

    @Test
    public void testGuiceNamedVsInjectOne() throws Exception {
        Id<MyInterface> keyBasedId = GuiceId.of(Key.get(MyInterface.class, Names.named("test")));
        assertThat(Ids.newId(MyInterface.class, Names.named("test")), is(Ids.newId(MyInterface.class, Names.named("test"))));
        assertThat(keyBasedId, is(Ids.newId(MyInterface.class, Names.named("test"))));
    }

    @Test
    public void testGuiceIdAnnotationClassVsAnnotationInstance() throws Exception {
        Key<MyInterface> keyClass = Key.get(MyInterface.class, MyAnnotation.class);
        Key<MyInterface> keyInstance = Key.get(MyInterface.class, getAnnotation(MyAnnotation.class));
        Id<MyInterface> idClass = GuiceId.of(Key.get(MyInterface.class, MyAnnotation.class));
        Id<MyInterface> idInstance = GuiceId.of(Key.get(MyInterface.class, getAnnotation(MyAnnotation.class)));
        assertThat(keyClass, is(keyInstance));
        assertThat(idClass, is(idInstance));
    }

    @Retention(RUNTIME) @Qualifier
    static @interface MyAnnotation {}

    @Named("test") @MyAnnotation
    static class MyImpl {}

    static private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return MyImpl.class.getAnnotation(annotationClass);
    }
}
