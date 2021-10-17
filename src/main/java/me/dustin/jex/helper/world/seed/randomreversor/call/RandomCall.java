package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public abstract class RandomCall {
  public abstract void onAdded(ReverserDevice paramReverserDevice);
  
  public abstract boolean checkState(Rand paramRand);
}
