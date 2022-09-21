package com.afoxxvi.alopex.util;

import java.util.Objects;

public class Pair<A, B> {
    public A a;
    public B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public Pair<A, B> copy() {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> triplet = (Pair<?, ?>) o;
        return Objects.equals(a, triplet.a) && Objects.equals(b, triplet.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
