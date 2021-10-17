package me.dustin.jex.helper.world.seed.randomreversor.math.lattice;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.*;
import java.util.function.*;
import java.util.*;

class SearchSpliterator implements Spliterator<BigVector>
{
    private final Deque<SearchNode> children;
    
    public SearchSpliterator(final Deque<SearchNode> children) {
        this.children = children;
    }
    
    @Override
    public boolean tryAdvance(final Consumer<? super BigVector> action) {
        while (!this.children.isEmpty()) {
            if (this.children.getFirst().spliterator().tryAdvance(action)) {
                return true;
            }
            this.children.removeFirst();
        }
        return false;
    }
    
    @Override
    public void forEachRemaining(final Consumer<? super BigVector> action) {
        while (!this.children.isEmpty()) {
            this.children.removeFirst().spliterator().forEachRemaining(action);
        }
    }
    
    @Override
    public Spliterator<BigVector> trySplit() {
        if (this.children.isEmpty()) {
            return null;
        }
        if (this.children.size() != 1) {
            final int count = this.children.size() / 2;
            final Deque<SearchNode> split = new LinkedList<SearchNode>();
            for (int i = 0; i < count; ++i) {
                split.addLast(this.children.removeFirst());
            }
            return new SearchSpliterator(split);
        }
        final Spliterator<BigVector> child = this.children.getFirst().spliterator();
        if (child != null) {
            return child.trySplit();
        }
        return null;
    }
    
    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }
    
    @Override
    public int characteristics() {
        return 1297;
    }
}
