package me.wbars.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionsUtils {
    private CollectionsUtils() {}
    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        return Arrays.stream(lists)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}