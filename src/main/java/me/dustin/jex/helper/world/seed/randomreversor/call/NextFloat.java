package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class NextFloat extends RandomCall {
  private final float min;
  private final float max;
  private final boolean minInclusive;
  private final boolean maxInclusive;
  
  protected NextFloat(float min, float max, boolean minInclusive, boolean maxInclusive) {
    this.min = min;
    this.max = max;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
  }

  public static NextFloat withValue(float value) { return inRange(value, value); }

  public static NextFloat inRange(float min, float max) { return inRange(min, max, true, false); }

  public static NextFloat inRange(float min, float max, boolean minInclusive, boolean maxInclusive) { return new NextFloat(min, max, minInclusive, maxInclusive); }

  public static Skip consume(int numSeeds) { return Skip.withCount(numSeeds); }

  public void onAdded(ReverserDevice device) {
    float minInc = this.min;
    float maxInc = this.max;
    
    if (!this.minInclusive) {
      minInc = Math.nextUp(this.min);
    }
    
    if (this.maxInclusive) {
      maxInc = Math.nextUp(this.max);
    }

    
    long minLong = (long)StrictMath.ceil((minInc * 1.6777216E7F));
    long maxLong = (long)StrictMath.ceil((maxInc * 1.6777216E7F)) - 1L;
    
    if (maxLong < minLong) {
      throw new IllegalArgumentException("call has no valid range");
    }
    
    device.processCall(Next.inBitsRange(24, minLong, maxLong + 1L));
  }

  
  public boolean checkState(Rand rand) {
    float value = rand.nextFloat();
    
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
