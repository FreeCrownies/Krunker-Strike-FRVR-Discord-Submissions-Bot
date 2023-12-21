package core.utils;

import core.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectMenuUtil {

    /**
     * @param selectedBefore list of selected items before the change
     * @param selectedAfter  list of selected items after the change
     * @param <T>            type of the items
     * @return a pair of lists, the first one contains the newly selected items, the second one contains the newly unselected items
     */
    public static <T> Pair<List<T>, List<T>> getSelectedUnselected(List<T> selectedBefore, List<T> selectedAfter) {
        List<T> selected = new ArrayList<>();
        List<T> unselected = new ArrayList<>();
        for (T t : selectedBefore) {
            if (!selectedAfter.contains(t)) {
                unselected.add(t);
            }
        }
        for (T t : selectedAfter) {
            if (!selectedBefore.contains(t)) {
                selected.add(t);
            }
        }
        return new Pair<>(selected, unselected);
    }

}
