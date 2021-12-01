package me.dustin.jex.helper.world.seed.randomreversor.util;

import java.util.*;

public final class Pair<A, B>
{
    private final A a;
    private final B b;
    
    public Pair(final A a, final B b) {
        this.a = a;
        this.b = b;
    }
    
    public Pair(final Pair<? extends A, ? extends B> other) {
        this(other.a, other.b);
    }
    
    public A getFirst() {
        return this.a;
    }
    
    public B getSecond() {
        return this.b;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.a, this.b);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != Pair.class) {
            return false;
        }
        final Pair<?, ?> that = (Pair<?, ?>)other;
        return Objects.equals(this.a, that.a) && Objects.equals(this.b, that.b);
    }
    
    @Override
    public String toString() {
        return "(" + this.a + ", " + this.b + ")";
    }
}
