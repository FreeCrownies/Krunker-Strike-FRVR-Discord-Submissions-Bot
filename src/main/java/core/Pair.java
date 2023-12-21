package core;

import java.io.Serializable;

public class Pair<L, R> implements Serializable {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public static <L, R> Pair<L, R> of (L left, R right) {
        return new Pair<>(left, right);
    }

}