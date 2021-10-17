package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Mth;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class NextDouble extends RandomCall {
  private final double min;
  private final double max;
  private final boolean minInclusive;
  private final boolean maxInclusive;
  
  protected NextDouble(double min, double max, boolean minInclusive, boolean maxInclusive) {
    this.min = min;
    this.max = max;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
  }

  
  public static NextDouble withValue(double value) { return inRange(value, value); }

  public static NextDouble inRange(double min, double max) { return new NextDouble(min, max, true, false); }

  public static NextDouble inRange(double min, double max, boolean minInclusive, boolean maxInclusive) { return new NextDouble(min, max, minInclusive, maxInclusive); }

  public static Skip consume(int numSeeds) { return Skip.withCount(2 * numSeeds); }

  public void onAdded(ReverserDevice device) {
    double minInc = this.min;
    double maxInc = this.max;
    
    if (!this.minInclusive) {
      minInc = Math.nextUp(this.min);
    }
    
    if (this.maxInclusive) {
      maxInc = Math.nextUp(this.max);
    }
    
    long minLong = (long)StrictMath.ceil(minInc * 9.007199254740992E15D);
    long maxLong = (long)StrictMath.ceil(maxInc * 9.007199254740992E15D) - 1L;
    
    if (maxLong < minLong) {
      throw new IllegalArgumentException("call has no valid range");
    }
    
    device.processCall(Next.inBitsRange(26, minLong >> 27, (maxLong >> 27) + 1L));
    
    if (minLong >>> 27 == maxLong >>> 27) {
      device.processCall(Next.inBitsRange(27, minLong & Mth.mask(21), maxLong & Mth.mask(21)));
    } else {
      device.processCall(Skip.withCount(1));
    } 
  }

  
  public boolean checkState(Rand rand) {
    double value = rand.nextDouble();
    
    if (this.minInclusive)
    { if (value < this.min) return false;
       }
    else if (value <= this.min) { return false; }

    
    if (this.maxInclusive) {
      return (value <= this.max);
    }
    return (value < this.max);
  }
}
