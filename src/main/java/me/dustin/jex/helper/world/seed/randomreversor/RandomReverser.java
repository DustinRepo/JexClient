package me.dustin.jex.helper.world.seed.randomreversor;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigFraction;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigMatrix;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigVector;
import me.dustin.jex.helper.world.seed.randomreversor.math.lattice.Enumerate;
import me.dustin.jex.helper.world.seed.randomreversor.math.lattice.LLL;
import me.dustin.jex.helper.world.seed.randomreversor.util.LCG;
import me.dustin.jex.helper.world.seed.randomreversor.util.Mth;
import me.dustin.jex.helper.world.seed.randomreversor.util.Rand;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.LongStream;

public class RandomReverser
{
  private static final BigInteger MOD = BigInteger.valueOf(281474976710656L);
  private static final BigInteger MULT = BigInteger.valueOf(25214903917L);


  private boolean verbose = false;

  private int dimensions = 0;
  private ArrayList<Long> mins = new ArrayList();
  private ArrayList<Long> maxes = new ArrayList();
  private ArrayList<Integer> callIndices = new ArrayList();
  private int currentCallIndex = 0;
  
  private BigMatrix lattice;
  
  public LongStream findAllValidSeeds() {
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
      .mapToLong(BigInteger::longValue)
      .map(r::nextSeed);
  }
  
  private void createLattice() {
    if (this.verbose)
      System.out.println("Call Indices: " + this.callIndices); 
    if (this.mins.size() != this.dimensions || this.maxes.size() != this.dimensions || this.callIndices.size() != this.dimensions) {
      return;
    }

    
    BigInteger[] sideLengths = new BigInteger[this.dimensions];
    
    for (int i = 0; i < this.dimensions; i++) {
      sideLengths[i] = BigInteger.valueOf(((Long)this.maxes.get(i)).longValue() - ((Long)this.mins.get(i)).longValue() + 1L);
    }
    
    BigInteger lcm = BigInteger.ONE;
    for (int i = 0; i < this.dimensions; i++) {
      lcm = Mth.lcm(lcm, sideLengths[i]);
    }
    
    BigMatrix scales = new BigMatrix(this.dimensions, this.dimensions);
    for (int i = 0; i < this.dimensions; i++) {
      for (int j = 0; j < this.dimensions; j++)
        scales.set(i, j, BigFraction.ZERO); 
      scales.set(i, i, new BigFraction(lcm.divide(sideLengths[i])));
    } 
    
    BigMatrix unscaledLattice = new BigMatrix(this.dimensions, this.dimensions);
    for (int i = 0; i < this.dimensions; i++) {
      for (int j = 0; j < this.dimensions; j++)
        unscaledLattice.set(i, j, BigFraction.ZERO); 
      if (i == 0) {
        unscaledLattice.set(0, i, BigFraction.ONE);
      } else {
        unscaledLattice.set(0, i, unscaledLattice.get(0, i - 1));
        
        BigInteger tempMult = MULT.modPow(BigInteger.valueOf((((Integer)this.callIndices.get(i)).intValue() - ((Integer)this.callIndices.get(0)).intValue())), MOD);
        unscaledLattice.set(0, i, new BigFraction(tempMult));
        
        unscaledLattice.set(i, i, new BigFraction(MOD));
      } 
    } 
    BigMatrix scaledLattice = unscaledLattice.multiply(scales);
    LLL.Params params = (new LLL.Params()).setDelta(0.99D).setDebug(false);
    if (this.verbose) {
      System.out.println("Reducing:\n" + scaledLattice.toPrettyString());
    }
    LLL.Result result = LLL.reduce(scaledLattice, params);
    
    if (this.verbose) {
      System.out.println("Found Reduced Scaled Basis:\n" + result.getReducedBasis().toPrettyString());
      System.out.println("Found Reduced Basis:\n" + result.getTransformations().multiply(unscaledLattice).toPrettyString());
    } 

    
    this.lattice = result.getTransformations().multiply(unscaledLattice);
  }

  
  private void addMeasuredSeed(long min, long max) {
    this.mins.add(Long.valueOf(min));
    this.maxes.add(Long.valueOf(max));
    this.dimensions++;
    this.currentCallIndex++;
    this.callIndices.add(Integer.valueOf(this.currentCallIndex));
  }

  
  private void addUnmeasuredSeeds(int numSeeds) { this.currentCallIndex += numSeeds; }


  
  public void addNextIntCall(int n, int min, int max) {
    if ((n & -n) == n) {
      int log = Long.numberOfTrailingZeros(n);
      addMeasuredSeed(min * (1L << 48 - log), (max + 1) * (1L << 48 - log) - 1L);
    } else {
      
      System.err.println("Reversal now has small chance of failure");
      
      consumeNextIntCalls(1);
    } 
  }

  
  public void addNextIntCall(int min, int max) { addMeasuredSeed(min * 65536L, (max + 1) * 65536L - 1L); }



  
  public void consumeNextIntCalls(int numCalls) { addUnmeasuredSeeds(numCalls); }

  
  public void addNextBooleanCall(boolean value) {
    if (value) {
      addNextIntCall(2, 1, 1);
    } else {
      addNextIntCall(2, 0, 0);
    } 
  }

  
  public void consumeNextBooleanCalls(int numCalls) { addUnmeasuredSeeds(numCalls); }










  
  public void addNextFloatCall(float min, float max, boolean minInclusive, boolean maxInclusive) {
    float minInc = min;
    float maxInc = max;
    
    if (!minInclusive) {
      minInc = Math.nextUp(min);
    }
    
    if (maxInclusive) {
      maxInc = Math.nextUp(max);
    }

    
    long minLong = (long)StrictMath.ceil((minInc * 1.6777216E7F));
    long maxLong = (long)StrictMath.ceil((maxInc * 1.6777216E7F)) - 1L;
    
    if (maxLong < minLong) {
      throw new IllegalArgumentException("call has no valid range");
    }
    
    long minSeed = minLong << 24;
    long maxSeed = maxLong << 24 | 0xFFFFFFL;
    
    addMeasuredSeed(minSeed, maxSeed);
  }







  
  public void addNextFloatCall(float min, float max) { addNextFloatCall(min, max, true, false); }


  
  public void consumeNextFloatCalls(int numCalls) { addUnmeasuredSeeds(numCalls); }

  
  public void addNextLongCall(long min, long max) {
    long maxFirstSeed, minFirstSeed;
    boolean minSignBit = ((min & 0xFFFFFFFF80000000L) != 0L);
    boolean maxSignBit = ((max & 0xFFFFFFFF80000000L) != 0L);

    
    if (minSignBit) {
      minFirstSeed = (min >>> 32) + 1L << 16;
    } else {
      minFirstSeed = min >>> 32 << 16;
    } 
    
    if (maxSignBit) {
      maxFirstSeed = ((max >>> 32) + 2L << 16) - 1L;
    } else {
      maxFirstSeed = ((max >>> 32) + 1L << 16) - 1L;
    } 
    addMeasuredSeed(minFirstSeed, maxFirstSeed);
    if (min >>> 32 == max >>> 32) {
      addMeasuredSeed((min & 0xFFFFFFFFL) << 16, ((max & 0xFFFFFFFFL) + 1L << 16) - 1L);
    } else {
      addUnmeasuredSeeds(1);
    } 
  }


  
  public void consumeNextLongCalls(int numCalls) { addUnmeasuredSeeds(2 * numCalls); }










  
  public void addNextDoubleCall(double min, double max, boolean minInclusive, boolean maxInclusive) {
    double minInc = min;
    double maxInc = max;
    
    if (!minInclusive) {
      minInc = Math.nextUp(min);
    }
    
    if (maxInclusive) {
      maxInc = Math.nextUp(max);
    }

    
    long minLong = (long)StrictMath.ceil(minInc * 9.007199254740992E15D);
    long maxLong = (long)StrictMath.ceil(maxInc * 9.007199254740992E15D) - 1L;
    
    if (maxLong < minLong) {
      throw new IllegalArgumentException("call has no valid range");
    }
    
    long minSeed1 = minLong >> 27 << 22;
    long maxSeed1 = maxLong >> 27 << 22 | 0x3FFFFFL;
    
    addMeasuredSeed(minSeed1, maxSeed1);
    
    if (minLong >>> 27 == maxLong >>> 27) {
      long minSeed2 = (minLong & 0x7FFFFFFL) << 21;
      long maxSeed2 = (maxLong & 0x7FFFFFFL) << 21 | 0x1FFFFFL;
      
      addMeasuredSeed(minSeed2, maxSeed2);
    } else {
      addUnmeasuredSeeds(1);
    } 
  }







  
  public void addNextDoubleCall(double min, double max) { addNextDoubleCall(min, max, true, false); }


  
  public void consumeNextDoubleCalls(int numCalls) { addUnmeasuredSeeds(2 * numCalls); }


  
  public void setVerbose(boolean verbose) { this.verbose = verbose; }
}
