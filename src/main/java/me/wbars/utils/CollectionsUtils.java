package me.wbars.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionsUtils {
    private CollectionsUtils() {}
    public static <T> List<T> merge(List<T> first, List<T> second) {
        ArrayList<T> result = new ArrayList<>();
        result.addAll(first);
        result.addAll(second);
        return result;
    }
}
