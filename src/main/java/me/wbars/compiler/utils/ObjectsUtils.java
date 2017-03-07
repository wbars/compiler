package me.wbars.compiler.utils;

import me.wbars.compiler.semantic.models.types.TypeRegistry;

public class ObjectsUtils {
    public static <T> T tryCast(Object object, Class<T> clazz) {
        return clazz.isInstance(object) ? clazz.cast(object) : null;
    }

    public static <T> T firstNonNull(T object1, T object2) {
        return object1 != null ? object1 : object2;
    }
}
