package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.LCG;
import me.dustin.jex.helper.world.seed.randomreversor.util.Mth;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class Next extends RandomCall {
  private final long min;
  private final long max;
  
  protected Next(long min, long max) {
    this.min = min;
    this.max = max;
  }

  public static Next inRange(long min, long max) { return new Next(min, max); }
  public static Next inBitsRange(int bits, long min, long max) { return new Next(min << 48 - bits, (max << 48 - bits) - 1L); }

  public void onAdded(ReverserDevice device) {
    device.mins.add(Long.valueOf(this.min));
    device.maxes.add(Long.valueOf(this.max));
    device.dimensions++;
    device.currentCallIndex++;
    device.callIndices.add(Integer.valueOf(device.currentCallIndex));
    device.estimatedSeeds *= (this.max - this.min + 1L) / Mth.pow2(48);
  }

  
  public boolean checkState(Rand rand) {
    rand.advance(LCG.JAVA);
    long seed = rand.getSeed();
    return (seed >= this.min && seed <= this.max);
  }
}
