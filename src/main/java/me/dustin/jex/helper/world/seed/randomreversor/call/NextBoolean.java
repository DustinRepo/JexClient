package me.dustin.jex.helper.world.seed.randomreversor.call;

import me.dustin.jex.helper.world.seed.randomreversor.ReverserDevice;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class NextBoolean extends RandomCall {
  private final boolean value;
  
  protected NextBoolean(boolean value) { this.value = value; }

  public static NextBoolean withValue(boolean value) { return new NextBoolean(value); }

  public static Skip consume(int numSeeds) { return NextInt.consume(2, numSeeds); }

  public void onAdded(ReverserDevice device) { device.processCall(NextInt.withValue(2, this.value ? 1 : 0)); }

  public boolean checkState(Rand rand) { return (rand.nextBoolean() == this.value); }
}
