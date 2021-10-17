package me.dustin.jex.helper.world.seed.randomreversor.math.lattice;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigFraction;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigMatrix;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigVector;
import me.dustin.jex.helper.world.seed.randomreversor.math.optimize.Optimize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Enumerate
{
    public static Stream<BigVector> enumerate(final BigMatrix basis, final BigVector origin, final Optimize constraints) {
        final int rootSize = basis.getRowCount();
        final BigMatrix rootInverse = basis.inverse();
        final BigVector rootOrigin = rootInverse.multiply(origin);
        final BigVector rootFixed = new BigVector(rootSize);
        final Optimize rootConstraints = constraints.copy();
        final List<BigFraction> widths = new ArrayList<BigFraction>();
        final List<Integer> order = new ArrayList<Integer>();
        for (int j = 0; j < rootSize; ++j) {
            final BigFraction min = constraints.copy().minimize(rootInverse.getRow(j)).getSecond();
            final BigFraction max = constraints.copy().maximize(rootInverse.getRow(j)).getSecond();
            widths.add(max.subtract(min));
            order.add(j);
        }
        order.sort(Comparator.comparing(i -> widths.get(i)));
        final SearchNode root = new SearchNode(rootSize, 0, rootInverse, rootOrigin, rootFixed, rootConstraints, order);
        return StreamSupport.stream(root.spliterator(), true).map((Function<? super BigVector, ?>)basis::multiply).map((Function<? super Object, ? extends BigVector>)origin::add);
    }

    public static Stream<BigVector> enumerate(final BigMatrix basis, final BigVector lower, final BigVector upper, final BigVector origin) {
        final Optimize.Builder builder = Optimize.Builder.ofSize(basis.getRowCount());
        for (int i = 0; i < basis.getRowCount(); ++i) {
            builder.withLowerBound(i, lower.get(i)).withUpperBound(i, upper.get(i));
        }
        return enumerate(basis, origin, builder.build());
    }
}
