package me.dustin.jex.helper.world.seed.randomreversor;

import java.math.*;
import me.dustin.jex.helper.world.seed.randomreversor.call.*;
import java.util.*;
import java.util.stream.*;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.*;
import me.dustin.jex.helper.world.seed.randomreversor.util.*;
import java.util.function.*;
import me.dustin.jex.helper.world.seed.randomreversor.math.lattice.*;

public class ReverserDevice
{
  private static final BigInteger MOD;
  private static final BigInteger MULT;
  private BigMatrix lattice;
  public ArrayList<Long> mins;
  public ArrayList<Long> maxes;
  public ArrayList<Integer> callIndices;
  public int currentCallIndex;
  public int dimensions;
  private boolean verbose;
  private List<RandomCall> calls;
  public double estimatedSeeds;

  public ReverserDevice() {
    this.calls = new ArrayList<RandomCall>();
    this.estimatedSeeds = (double)Mth.pow2(48);
    this.verbose = false;
    this.dimensions = 0;
    this.mins = new ArrayList<Long>();
    this.maxes = new ArrayList<Long>();
    this.callIndices = new ArrayList<Integer>();
    this.currentCallIndex = 0;
  }

  public ReverserDevice processCall(final RandomCall call) {
    call.onAdded(this);
    return this;
  }

  public ReverserDevice addCall(final RandomCall call) {
    this.calls.add(call);
    this.processCall(call);
    return this;
  }

  public ReverserDevice next(final long min, final long max) {
    return this.addCall(Next.inRange(min, max));
  }

  public ReverserDevice nextBits(final int bits, final long min, final long max) {
    return this.addCall(Next.inBitsRange(bits, min, max));
  }

  public ReverserDevice skip(final int numSeeds) {
    return this.addCall(Skip.withCount(numSeeds));
  }

  public ReverserDevice filterSkip(final Predicate<Rand> filter) {
    return this.addCall(FilteredSkip.filter(filter));
  }

  public ReverserDevice next(final int bits, final long min, final long max) {
    return this.addCall(Next.inBitsRange(bits, min, max));
  }

  public ReverserDevice nextBoolean(final boolean value) {
    return this.addCall(NextBoolean.withValue(value));
  }

  public ReverserDevice skipNextBoolean(final int numSeeds) {
    return this.addCall(NextBoolean.consume(numSeeds));
  }

  public ReverserDevice nextInt(final int bound, final int value) {
    return this.addCall(NextInt.withValue(bound, value));
  }

  public ReverserDevice nextInt(final int bound, final int min, final int max) {
    return this.addCall(NextInt.inRange(bound, min, max));
  }

  public ReverserDevice skipNextInt(final int bound, final int numSeeds) {
    return this.addCall(NextInt.consume(bound, numSeeds));
  }

  public ReverserDevice nextIntUnbounded(final int value) {
    return this.addCall(NextInt.withValue(value));
  }

  public ReverserDevice nextIntUnbounded(final int min, final int max) {
    return this.addCall(NextInt.inRange(min, max));
  }

  public ReverserDevice skipNextIntUnbounded(final int numSeeds) {
    return this.addCall(NextInt.consume(numSeeds));
  }

  public ReverserDevice nextFloat(final float value) {
    return this.addCall(NextFloat.withValue(value));
  }

  public ReverserDevice nextFloat(final float min, final float max) {
    return this.addCall(NextFloat.inRange(min, max));
  }

  public ReverserDevice nextFloat(final float min, final float max, final boolean minInclusive, final boolean maxInclusive) {
    return this.addCall(NextFloat.inRange(min, max, minInclusive, maxInclusive));
  }

  public ReverserDevice skipNextFloat(final int numSeeds) {
    return this.addCall(NextFloat.consume(numSeeds));
  }

  public ReverserDevice nextLong(final long value) {
    return this.addCall(NextLong.withValue(value));
  }

  public ReverserDevice nextLong(final long min, final long max) {
    return this.addCall(NextLong.inRange(min, max));
  }

  public ReverserDevice skipNextLong(final int numSeeds) {
    return this.addCall(NextLong.consume(numSeeds));
  }

  public ReverserDevice nextDouble(final double value) {
    return this.addCall(NextDouble.withValue(value));
  }

  public ReverserDevice nextDouble(final double min, final double max) {
    return this.addCall(NextDouble.inRange(min, max));
  }

  public ReverserDevice nextDouble(final double min, final double max, final boolean minInclusive, final boolean maxInclusive) {
    return this.addCall(NextDouble.inRange(min, max, minInclusive, maxInclusive));
  }

  public ReverserDevice skipNextDouble(final int numSeeds) {
    return this.addCall(NextDouble.consume(numSeeds));
  }

  public ArrayList<Long> findAllValidSeeds() {
    final ArrayList<Long> results = this.streamSeeds().collect(Collectors.toCollection(ArrayList::new));
    if (this.verbose) {
      for (final long seed : results) {
        System.out.println("found: " + seed);
      }
    }
    return results;
  }

  public Stream<Long> streamSeeds() {
    createLattice();
    BigVector lower = new BigVector(this.dimensions);
    BigVector upper = new BigVector(this.dimensions);
    BigVector offset = new BigVector(this.dimensions);
    Rand rand = Rand.ofInternalSeed(0L);

    for (int i = 0; i < this.dimensions; i++) {
      lower.set(i, new BigFraction(((Long)this.mins.get(i)).longValue()));
      upper.set(i, new BigFraction(((Long)this.maxes.get(i)).longValue()));
      offset.set(i, new BigFraction(rand.getSeed()));

      if (i != this.dimensions - 1) {
        rand.advance(((Integer)this.callIndices.get(i + 1)).intValue() - ((Integer)this.callIndices.get(i)).intValue());
      }
    }

    if (this.verbose) {
      System.out.println("Mins: " + lower);
      System.out.println("Maxes: " + upper);
      System.out.println("Offsets: " + offset);
    }

    LCG r = LCG.JAVA.combine(-((Integer)this.callIndices.get(0)).intValue());

    return Enumerate.enumerate(this.lattice.transpose(), lower, upper, offset)
            .map(vec -> vec.get(0))
            .map(BigFraction::getNumerator)
            .map(BigInteger::longValue)
            .map(r::nextSeed)
            .filter(seed -> {
              Rand rr = Rand.ofInternalSeed(seed.longValue());

              for (RandomCall call : this.calls) {
                if (!call.checkState(rr)) {
                  return false;
                }
              }

              return true;
            });
  }

  private void createLattice() {
    if (this.verbose) {
      System.out.println("Call Indices: " + this.callIndices);
    }
    if (this.mins.size() != this.dimensions || this.maxes.size() != this.dimensions || this.callIndices.size() != this.dimensions) {
      return;
    }
    final BigInteger[] sideLengths = new BigInteger[this.dimensions];
    for (int i = 0; i < this.dimensions; ++i) {
      sideLengths[i] = BigInteger.valueOf(this.maxes.get(i) - this.mins.get(i) + 1L);
    }
    BigInteger lcm = BigInteger.ONE;
    for (int j = 0; j < this.dimensions; ++j) {
      lcm = Mth.lcm(lcm, sideLengths[j]);
    }
    final BigMatrix scales = new BigMatrix(this.dimensions, this.dimensions);
    for (int k = 0; k < this.dimensions; ++k) {
      for (int l = 0; l < this.dimensions; ++l) {
        scales.set(k, l, BigFraction.ZERO);
      }
      scales.set(k, k, new BigFraction(lcm.divide(sideLengths[k])));
    }
    final BigMatrix unscaledLattice = new BigMatrix(this.dimensions, this.dimensions);
    for (int m = 0; m < this.dimensions; ++m) {
      for (int j2 = 0; j2 < this.dimensions; ++j2) {
        unscaledLattice.set(m, j2, BigFraction.ZERO);
      }
      if (m == 0) {
        unscaledLattice.set(0, m, BigFraction.ONE);
      }
      else {
        unscaledLattice.set(0, m, unscaledLattice.get(0, m - 1));
        final BigInteger tempMult = ReverserDevice.MULT.modPow(BigInteger.valueOf(this.callIndices.get(m) - this.callIndices.get(0)), ReverserDevice.MOD);
        unscaledLattice.set(0, m, new BigFraction(tempMult));
        unscaledLattice.set(m, m, new BigFraction(ReverserDevice.MOD));
      }
    }
    final BigMatrix scaledLattice = unscaledLattice.multiply(scales);
    final LLL.Params params = new LLL.Params().setDelta(0.99).setDebug(false);
    if (this.verbose) {
      System.out.println("Reducing:\n" + scaledLattice.toPrettyString());
    }
    final LLL.Result result = LLL.reduce(scaledLattice, params);
    if (this.verbose) {
      System.out.println("Found Reduced Scaled Basis:\n" + result.getReducedBasis().toPrettyString());
      System.out.println("Found Reduced Basis:\n" + result.getTransformations().multiply(unscaledLattice).toPrettyString());
    }
    this.lattice = result.getTransformations().multiply(unscaledLattice);
  }

  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  static {
    MOD = BigInteger.valueOf(281474976710656L);
    MULT = BigInteger.valueOf(25214903917L);
  }
}