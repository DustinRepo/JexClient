package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Mth;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class NextLong extends RandomCall {
  private final long min;
  private final long max;
  
  protected NextLong(long min, long max) {
    this.min = min;
    this.max = max;
  }

  public static NextLong withValue(long value) { return inRange(value, value); }

  public static NextLong inRange(long min, long max) { return new NextLong(min, max); }

  public static Skip consume(int numSeeds) { return Skip.withCount(2 * numSeeds); }

  public void onAdded(ReverserDevice device) {
    boolean minSignBit = ((this.min & Mth.pow2(31)) != 0L);
    boolean maxSignBit = ((this.max & Mth.pow2(31)) != 0L);
    
    device.processCall(Next.inBitsRange(32, (this.min >>> 32) + (minSignBit ? 1 : 0), (this.max >>> 32) + (maxSignBit ? 2 : 1)));
    
    if (this.min >>> 32 == this.max >>> 32) {
      device.processCall(Next.inBitsRange(32, this.min & Mth.mask(32), (this.max & Mth.mask(32)) + 1L));
    } else {
      device.processCall(Skip.withCount(1));
    } 
  }

  
  public boolean checkState(Rand rand) {
    long value = rand.nextLong();
    return (value >= this.min && value <= this.max);
  }
}
