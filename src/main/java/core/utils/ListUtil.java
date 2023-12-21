package core.utils;

import core.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ListUtil {

    public static <K, V> List<Pair<K, V>> mapToPairList(Map<K, V> map) {
        return map.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public static <K, V> List<Pair<K, V>> mapToPairList(Map<K, V> map, Comparator<Pair<K, V>> comparator) {
        return map.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).sorted(comparator).collect(Collectors.toList());
    }

}