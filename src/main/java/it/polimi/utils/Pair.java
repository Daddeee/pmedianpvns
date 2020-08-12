package it.polimi.utils;

public class Pair<A,B> {
    private A first;
    private B second;

    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
