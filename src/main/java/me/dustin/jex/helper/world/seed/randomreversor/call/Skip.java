package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.LCG;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class Skip extends RandomCall {
  private final int numSeeds;
  private final LCG skipLCG;
  
  protected Skip(int numSeeds) {
    this.numSeeds = numSeeds;
    this.skipLCG = LCG.JAVA.combine(numSeeds);
  }

  public static Skip withCount(int numSeeds) { return new Skip(numSeeds); }

  public void onAdded(ReverserDevice device) { device.currentCallIndex += this.numSeeds; }

  public boolean checkState(Rand rand) {
    rand.advance(this.skipLCG);
    return true;
  }
}
