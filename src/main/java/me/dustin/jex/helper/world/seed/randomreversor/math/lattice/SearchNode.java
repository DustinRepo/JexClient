package me.dustin.jex.helper.world.seed.randomreversor.math.lattice;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigFraction;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigMatrix;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigVector;
import me.dustin.jex.helper.world.seed.randomreversor.math.optimize.Optimize;

import java.math.BigInteger;
import java.util.*;

class SearchNode
{
    private final int size;
    private final int depth;
    private final BigMatrix inverse;
    private final BigVector origin;
    private final BigVector fixed;
    private final Optimize constraints;
    private final List<Integer> order;
    private Spliterator<BigVector> spliterator;
    
    public SearchNode(final int size, final int depth, final BigMatrix inverse, final BigVector origin, final BigVector fixed, final Optimize constraints, final List<Integer> order) {
        this.size = size;
        this.depth = depth;
        this.inverse = inverse;
        this.origin = origin;
        this.fixed = fixed;
        this.constraints = constraints;
        this.order = order;
    }
    
    private void initialize() {
        if (this.depth == this.size) {
            this.spliterator = Collections.singleton(this.fixed).spliterator();
            return;
        }
        final int index = this.order.get(this.depth);
        final Deque<SearchNode> children = new LinkedList<SearchNode>();
        final BigVector gradient = this.inverse.getRow(index);
        final BigFraction offset = this.origin.get(index);
        for (BigInteger min = this.constraints.copy().minimize(gradient).getSecond().subtract(offset).ceil(), max = this.constraints.copy().maximize(gradient).getSecond().subtract(offset).floor(); min.compareTo(max) <= 0; min = min.add(BigInteger.ONE)) {
            final Optimize next = this.constraints.withStrictBound(gradient, new BigFraction(min).add(offset));
            this.fixed.set(index, new BigFraction(min));
            children.addLast(new SearchNode(this.size, this.depth + 1, this.inverse, this.origin, this.fixed.copy(), next, this.order));
        }
        this.spliterator = new SearchSpliterator(children);
    }
    
    public Spliterator<BigVector> spliterator() {
        if (this.spliterator == null) {
            this.initialize();
        }
        return this.spliterator;
    }
}
