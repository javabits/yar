package org.javabits.yar;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;
import static org.javabits.yar.Annotations.checkRuntimeRetention;

/**
 * This class provides utility methods to construct and deal with {@link Id}.
 * This class provide 3 main ways to build {@code Id}:
 * <ul>
 * <li>from {@code Class}</li>
 * <li>from {@code TypeToken}</li>
 * <li>from {@code Type}</li>
 * </ul>
 * As follow:
 * <h3>Class based construction:</h3>
 * <pre>
 *     Ids.newId(MyInterface.class);
 *     Ids.newId(MyInterface.class, MyAnnotation.class);
 *     Ids.newId(MyInterface.class, Names.named("my-name"));
 * </pre>
 * <h3>TypeToken based construction:</h3>
 * <pre>
 *     Ids.newId(new TypeToken<List<MyInterface>>(){});
 *     Ids.newId(new TypeToken<List<MyInterface>>(){}, MyAnnotation.class);
 *     Ids.newId(new TypeToken<List<MyInterface>>(){}, Names.named("my-name"));
 * </pre>
 * <h3>Type based construction</h3>
 * <pre>
 *     Ids.newId(aMethod.getGenericParameterTypes()[0]);
 *     Ids.newId(aMethod.getGenericParameterTypes()[0], MyAnnotation.class);
 *     Ids.newId(aMethod.getGenericParameterTypes()[0], Names.named("my-name"));
 * </pre>
 *
 * @author Romain Gilles
 */
public final class Ids {
    private Ids() {
        throw new AssertionError("Not for you!");
    }

    /**
     * Returns a new {@link Id} for the given type.
     *
     * @param type the {@code Type} from which the {@code Id} must be constructed.
     * @param <T>  the type of the Id.
     * @return an new {@code Id} based on the given type.
     */
    public static <T> Id<T> newId(final Class<T> type) {
        return newId(TypeToken.of(type));
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotationClass}.
     *
     * @param type            the type to which the new id has to be associated.
     * @param annotationClass the qualifying annotation type.
     * @param <T>             the type of the id.
     * @return an new {@code Id} based on the given type and annotation type.
     */
    public static <T> Id<T> newId(final Class<T> type, final Class<? extends Annotation> annotationClass) {
        return newId(TypeToken.of(type), annotationClass);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotation} instance.
     *
     * @param type       the type to which the new id has to be associated.
     * @param annotation the qualifying annotation.
     * @param <T>        the type of the id.
     * @return an new {@code Id} based on the given type and annotation.
     */
    public static <T> Id<T> newId(final Class<T> type, final Annotation annotation) {
        return newId(TypeToken.of(type), annotation);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type}.
     *
     * @param type the type to which the new id has to be associated.
     * @param <T>  the type of the id.
     * @return an new {@code Id} based on the given type.
     */
    public static <T> Id<T> newId(final TypeToken<T> type) {
        return IdImpl.newId(type);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotationClass}.
     *
     * @param type            the type to which the new id has to be associated.
     * @param annotationClass the qualifying annotation type.
     * @param <T>             the type of the id.
     * @return an new {@code Id} based on the given type and annotation type.
     */
    public static <T> Id<T> newId(final TypeToken<T> type, final Class<? extends Annotation> annotationClass) {
        return IdImpl.newId(type, annotationClass);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotation} instance.
     *
     * @param type       the type to which the new id has to be associated.
     * @param annotation the qualifying annotation.
     * @param <T>        the type of the id.
     * @return an new {@code Id} based on the given type and annotation.
     */
    public static <T> Id<T> newId(final TypeToken<T> type, final Annotation annotation) {
        return IdImpl.newId(type, annotation);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type}.
     *
     * @param type the type to which the new id has to be associated.
     * @return an new {@code Id} based on the given type.
     */
    public static Id<?> newId(final Type type) {
        return newId(TypeToken.of(type));
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotationClass}.
     *
     * @param type            the type to which the new id has to be associated.
     * @param annotationClass the qualifying annotation type.
     * @return an new {@code Id} based on the given type and annotation type.
     */
    public static Id<?> newId(final Type type, final Class<? extends Annotation> annotationClass) {
        return newId(TypeToken.of(type), annotationClass);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotation} instance.
     *
     * @param type       the type to which the new id has to be associated.
     * @param annotation the qualifying annotation.
     * @return an new {@code Id} based on the given type and annotation.
     */
    public static Id<?> newId(final Type type, final Annotation annotation) {
        return newId(TypeToken.of(type), annotation);
    }

    static final class IdImpl<T> implements Id<T> {

        private final TypeToken<T> typeToken;
        private final AnnotationStrategy annotationStrategy;
        private final int hashCode;

        private IdImpl(TypeToken<T> typeToken, AnnotationStrategy annotationStrategy) {
            this.typeToken = requireNonNull(typeToken, "typeToken");
            this.annotationStrategy = requireNonNull(annotationStrategy, "annotationStrategy");
            this.hashCode = computeHashCode(typeToken, annotationStrategy);
        }

        private static <T> Id<T> newId(final TypeToken<T> type) {
            return new IdImpl<>(type, AbstractAnnotationStrategy.NULL_STRATEGY);
        }

        private static <T> Id<T> newId(final TypeToken<T> type, final Class<? extends Annotation> annotationClass) {
            return new IdImpl<>(type, AbstractAnnotationStrategy.strategyFor(annotationClass));
        }

        private static <T> Id<T> newId(final TypeToken<T> type, final Annotation annotation) {
            return new IdImpl<>(type, AbstractAnnotationStrategy.strategyFor(annotation));
        }

        @Override
        public Type type() {
            return typeToken.getType();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return annotationStrategy.getAnnotationType();
        }

        @Override
        public Annotation annotation() {
            return annotationStrategy.getAnnotation();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IdImpl id = (IdImpl) o;

            return annotationStrategy.equals(id.annotationStrategy) && typeToken.equals(id.typeToken);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        private int computeHashCode(TypeToken<T> typeToken, AnnotationStrategy annotationStrategy) {
            int result = requireNonNull(typeToken, "typeToken").hashCode();
            result = 31 * result + requireNonNull(annotationStrategy, "annotationStrategy").hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Id[type=" + typeToken + ", annotation=" + annotationStrategy + "]";
        }

        interface AnnotationStrategy {
            @Nullable
            Annotation getAnnotation();

            @Nullable
            Class<? extends Annotation> getAnnotationType();

            @Override
            int hashCode();

            @Override
            boolean equals(Object o);
        }

        static abstract class AbstractAnnotationStrategy implements AnnotationStrategy {

            static final AnnotationStrategy NULL_STRATEGY = new NullAnnotationStrategy();

            static AnnotationStrategy strategyFor(Class<? extends Annotation> annotationClass) {
                return new TypeAnnotationStrategy(annotationClass);
            }

            public static AnnotationStrategy strategyFor(Annotation annotation) {
                if (Annotations.isMarker(annotation)) {
                    return new TypeAnnotationStrategy(annotation.annotationType());
                }
                return new InstanceAnnotationStrategy(annotation);
            }
        }

        static final class NullAnnotationStrategy extends AbstractAnnotationStrategy {
            NullAnnotationStrategy() {
            }

            @Nullable
            @Override
            public Annotation getAnnotation() {
                return null;
            }

            @Override
            public Class<? extends Annotation> getAnnotationType() {
                return null;
            }

            @Override
            public String toString() {
                return "[none]";
            }

        }

        static final class TypeAnnotationStrategy extends AbstractAnnotationStrategy {

            private final Class<? extends Annotation> annotationClass;

            TypeAnnotationStrategy(Class<? extends Annotation> annotationClass) {
                this.annotationClass = checkRuntimeRetention(annotationClass, "annotationClass");
            }

            @Override
            public Annotation getAnnotation() {
                return null;
            }

            @Override
            public Class<? extends Annotation> getAnnotationType() {
                return annotationClass;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                TypeAnnotationStrategy that = (TypeAnnotationStrategy) o;

                return annotationClass.equals(that.annotationClass);
            }

            @Override
            public int hashCode() {
                return annotationClass.hashCode();
            }

            @Override
            public String toString() {
                return "@" + annotationClass.toString();
            }
        }

        static final class InstanceAnnotationStrategy extends AbstractAnnotationStrategy {

            private final Annotation annotation;

            InstanceAnnotationStrategy(Annotation annotation) {
                this.annotation = annotation;
            }

            @Nullable
            @Override
            public Annotation getAnnotation() {
                return annotation;
            }

            @Override
            public Class<? extends Annotation> getAnnotationType() {
                return annotation.annotationType();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                InstanceAnnotationStrategy that = (InstanceAnnotationStrategy) o;

                return annotation.equals(that.annotation);
            }

            @Override
            public int hashCode() {
                return annotation.hashCode();
            }

            @Override
            public String toString() {
                return annotation.toString();
            }
        }
    }
}
