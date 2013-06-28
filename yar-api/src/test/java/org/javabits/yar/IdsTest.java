package org.javabits.yar;

import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test cases on Id features
 *
 * @author Romain Gilles
 */
public class IdsTest {

    private final Type type = new TypeToken<List<MyInterface>>() {
    }.getType();

    /**
     * default smock test
     */
    @Test
    public void testNewIdClass() {
        assertThat(Ids.newId(MyInterface.class), is(not(nullValue())));
    }

    @Test
    public void testNewIdClassType() {
        assertThat(Ids.newId(MyInterface.class).type(), is((Type)MyInterface.class));
    }

    @Test
    public void testNewIdClassAnnotation() {
        assertThat(Ids.newId(MyInterface.class, getAnnotation()), is(not(nullValue())));
    }

    static private MyAnnotation getAnnotation() {
        return MyImpl.class.getAnnotation(MyAnnotation.class);
    }

    @Test
    public void testNewIdClassAnnotationType() {
        assertThat(Ids.newId(MyInterface.class, MyAnnotation.class).annotationType(), equalTo((Class)MyAnnotation.class));
        assertThat(Ids.newId(MyInterface.class, MyAnnotation.class).annotation(), is(nullValue()));
    }

    @Test
    public void testNewIdClassEqual() {
        Id<MyInterface> same = Ids.newId(MyInterface.class);
        assertThat(same, is(same));
        assertThat(same.equals(null), is(false));
        assertThat(same.equals("an other object type"), is(false));
        assertThat(Ids.newId(MyInterface.class), is(Ids.newId(MyInterface.class)));
        assertThat((Id)Ids.newId(MyInterface.class), is(not((Id)Ids.newId(List.class))));
    }

    @Test
    public void testNewIdClassAnnotationTypeEqual() {
        assertThat(Ids.newId(MyInterface.class, getAnnotation()), is(Ids.newId(MyInterface.class, getAnnotation())));
        assertThat(Ids.newId(MyInterface.class, getAnnotation()), is(not(Ids.newId(MyInterface.class))));
    }

    @Test
    public void testNewIdClassAnnotationEqual() {
        assertThat(Ids.newId(MyInterface.class, MyAnnotation.class), is(Ids.newId(MyInterface.class, MyAnnotation.class)));
        assertThat(Ids.newId(MyInterface.class, MyAnnotation.class), is(not(Ids.newId(MyInterface.class))));
    }

    @Test
    public void testNewIdType() {
        assertThat(Ids.newId(type), is(not(nullValue())));
    }

    @Test
    public void testNewIdTypeGetType() {
        assertThat(Ids.newId(type).type(), is(type));
    }

    @Test
    public void testNewIdTypeGetAnnotation() {
        assertThat(Ids.newId(type, getAnnotation()), is(not(nullValue())));
    }

    @Test
    public void testNewIdTypeGetAnnotationType() {
        assertThat(Ids.newId(type, MyAnnotation.class).annotationType(), equalTo((Class)MyAnnotation.class));
        assertThat(Ids.newId(type, MyAnnotation.class).annotation(), is(nullValue()));
    }

    @Test
    public void testNewIdTypeEqual() {
        assertThat((Id)Ids.newId(type), is((Id)Ids.newId(type)));
        assertThat((Id)Ids.newId(type), is(not((Id)Ids.newId(MyInterface.class))));
    }

    @Test
    public void testNewIdTypeAnnotationTypeEqual() {
        assertThat((Id)Ids.newId(type, getAnnotation()), is((Id)Ids.newId(type, getAnnotation())));
        assertThat((Id)Ids.newId(type, getAnnotation()), is(not((Id)Ids.newId(type))));
        assertThat((Id)Ids.newId(type, getAnnotation()), is(not((Id)Ids.newId(MyInterface.class))));
    }

    @Test
    public void testNewIdTypeAnnotationEqual() {
        assertThat((Id)Ids.newId(type, MyAnnotation.class), is((Id)Ids.newId(type, MyAnnotation.class)));
        assertThat((Id)Ids.newId(type, MyAnnotation.class), is(not((Id)Ids.newId(type))));
        assertThat((Id)Ids.newId(type, MyAnnotation.class), is(not((Id)Ids.newId(MyInterface.class))));
    }

    @Test
    public void testToStringOfTypeOnly() throws Exception {
        assertThat(Ids.newId(MyInterface.class).toString(), containsString(MyInterface.class.getSimpleName()));
    }

    static interface MyInterface {
    }

    @Retention(RUNTIME)
    static @interface MyAnnotation {}

    @MyAnnotation
    static class MyImpl{}
}
