package me.wbars.compiler.utils;

public class ObjectsUtils {
    public static <T> T tryCast(Object object, Class<T> clazz) {
        return clazz.isInstance(object) ? clazz.cast(object) : null;
    }

    public static <T> T firstNonNull(T object1, T object2) {
        return object1 != null ? object1 : object2;
    }

    public static String spaceConcat(Object s1, Object s2) {
        return s1 + " " + s2;
    }
}
