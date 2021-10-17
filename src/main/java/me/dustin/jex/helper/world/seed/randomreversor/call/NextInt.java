package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Mth;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

import java.util.function.Predicate;

public class NextInt extends RandomCall {
  private final int bound;
  private final int min;
  private final int max;
  private final boolean bounded;
  private Predicate<Integer> filter;
  
  protected NextInt(int bound, int min, int max, boolean bounded) {
    this.bound = bound;
    this.min = min;
    this.max = max;
    this.bounded = bounded;
  }
  
  public NextInt filter(Predicate<Integer> filter) {
    this.filter = filter;
    return this;
  }

  
  public Predicate<Integer> getFilter() { return this.filter; }


  
  public static NextInt withValue(int bound, int value) { return inRange(bound, value, value); }


  
  public static NextInt withValue(int value) { return inRange(value, value); }


  
  public static NextInt inRange(int bound, int min, int max) { return new NextInt(bound, min, max, true); }

  
  public static NextInt inRange(int min, int max) {
    long lower = min * Mth.pow2(16);
    long upper = (max + 1) * Mth.pow2(16) - 1L;
    return new NextInt(-1, min, max, false);
  }

  public static RandomCall consume(int numSeeds) { return Skip.withCount(numSeeds); }

  public static Skip consume(int bound, int numSeeds) { return Skip.withCount(numSeeds); }

  public void onAdded(ReverserDevice device) {
    if (!this.bounded) {
      device.processCall(Next.inBitsRange(32, this.min, (this.max + 1)));
    } else if (Mth.isPowerOf2(this.bound)) {
      int bits = Long.numberOfTrailingZeros(this.bound);
      device.processCall(Next.inBitsRange(bits, this.min, (this.max + 1)));
    } else {
      System.err.println("Reversal now has small chance of failure.");
      device.processCall(consume(this.bound, 1));
    } 
  }

  
  public boolean checkState(Rand rand) {
    int value = this.bounded ? rand.nextInt(this.bound) : rand.nextInt();
    if (value < this.min || value > this.max) return false; 
    if (getFilter() != null && !getFilter().test(Integer.valueOf(value))) return false; 
    return true;
  }
}
