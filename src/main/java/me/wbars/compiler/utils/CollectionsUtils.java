package me.wbars.compiler.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionsUtils {
    private CollectionsUtils() {
    }

    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        return Arrays.stream(lists)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static <T> T last(List<T> list) {
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    public static <T> T first(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> List<T> flatten(List<List<T>> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static <T> int findSublistIndex(List<T> base, List<T> search, List<Pair<Integer, Integer>> rangesForSkip) {
        return IntStream.range(0, base.size() - search.size()).boxed()
                .filter(i -> rangesForSkip.stream().noneMatch(p -> p.first() <= i && i <= p.second()))
                .filter(i -> base.subList(i, i + search.size()).equals(search))
                .findFirst().orElse(-1);
    }
}
