package me.dustin.jex.helper.world.seed.randomreversor.call;

import java.util.function.Predicate;

import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

public class FilteredSkip
  extends Skip
{
  private final Predicate<Rand> filter;
  
  protected FilteredSkip(Predicate<Rand> filter) {
    super(1);
    this.filter = filter;
  }

  
  public static FilteredSkip filter(Predicate<Rand> filter) { return new FilteredSkip(filter); }

  public boolean checkState(Rand rand) { return this.filter.test(rand); }
}
