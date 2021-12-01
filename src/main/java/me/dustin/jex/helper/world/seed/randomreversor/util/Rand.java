package me.dustin.jex.helper.world.seed.randomreversor.util;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.lang.reflect.*;

public class Rand
{
    private final LCG lcg;
    private long seed;
    
    private Rand(final LCG lcg) {
        this.lcg = lcg;
    }
    
    public static Rand ofInternalSeed(final LCG lcg, final long seed) {
        final Rand rand = new Rand(lcg);
        rand.setInternalSeed(seed);
        return rand;
    }
    
    public static Rand ofSeedScrambled(final LCG lcg, final long seed) {
        final Rand rand = new Rand(lcg);
        rand.setSeedScrambled(seed);
        return rand;
    }
    
    public static Rand ofInternalSeed(final long seed) {
        return ofInternalSeed(LCG.JAVA, seed);
    }
    
    public static Rand ofSeedScrambled(final long seed) {
        return ofSeedScrambled(LCG.JAVA, seed);
    }
    
    public static Rand copyOf(final Rand other) {
        final Rand rand = new Rand(other.lcg);
        rand.seed = other.seed;
        return rand;
    }
    
    public static Rand copyOf(final Random random) {
        if (random instanceof RandomWrapper) {
            return copyOf(((RandomWrapper)random).delegate);
        }
        if (random.getClass() == Random.class) {
            try {
                final AtomicLong seed = (AtomicLong)SeedFieldHolder.FIELD.get(random);
                return ofInternalSeed(seed.get());
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("Don't know how to Rand.copyOf() an instance of " + random.getClass().getName() + ", it may not even be an LCG!");
    }
    
    public long getSeed() {
        return this.seed;
    }
    
    public void setInternalSeed(final long seed) {
        this.seed = this.lcg.mod(seed);
    }
    
    public void setSeedScrambled(final long seed) {
        this.setInternalSeed(seed ^ LCG.JAVA.multiplier);
    }
    
    public int next(final int bits) {
        this.seed = this.lcg.nextSeed(this.seed);
        return (int)(this.seed >>> 48 - bits);
    }
    
    public void advance(final int calls) {
        this.advance(this.lcg.combine(calls));
    }
    
    public void advance(final LCG skip) {
        this.seed = skip.nextSeed(this.seed);
    }
    
    public boolean nextBoolean() {
        return this.next(1) == 1;
    }
    
    public int nextInt() {
        return this.next(32);
    }
    
    public int nextInt(final int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        if ((bound & -bound) == bound) {
            return (int)(bound * (long)this.next(31) >> 31);
        }
        int bits;
        int value;
        do {
            bits = this.next(31);
            value = bits % bound;
        } while (bits - value + (bound - 1) < 0);
        return value;
    }
    
    public float nextFloat() {
        return this.next(24) / 1.6777216E7f;
    }
    
    public long nextLong() {
        return ((long)this.next(32) << 32) + this.next(32);
    }
    
    public double nextDouble() {
        return (((long)this.next(27) << 27) + this.next(27)) / 1.8014398509481984E16;
    }
    
    public Random asRandomView() {
        return new RandomWrapper(this);
    }
    
    public Random copyToRandom() {
        return copyOf(this).asRandomView();
    }
    
    public Random copyToThreadSafeRandom() {
        if (!this.lcg.equals(LCG.JAVA)) {
            throw new UnsupportedOperationException("Rand.copyToThreadSafeRandom() only works for LCG.JAVA");
        }
        return new Random(this.seed ^ this.lcg.multiplier);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Rand)) {
            return false;
        }
        final Rand rand = (Rand)obj;
        return rand.getSeed() == this.getSeed();
    }
    
    @Override
    public String toString() {
        return "Rand{seed=" + this.seed + '}';
    }
    
    private static final class RandomWrapper extends Random
    {
        private final Rand delegate;
        
        private RandomWrapper(final Rand delegate) {
            this.delegate = delegate;
        }
        
        @Override
        protected int next(final int bits) {
            return this.delegate.next(bits);
        }
        
        @Override
        public void setSeed(final long seed) {
            this.delegate.setSeedScrambled(seed);
        }
        
        @Override
        public double nextGaussian() {
            throw new UnsupportedOperationException("Rand.asRandomView() and Rand.copyToRandom() do not support nextGaussian()! Use Rand.copyToThreadSafeRandom() instead.");
        }
    }
    
    private static class SeedFieldHolder
    {
        static final Field FIELD;
        
        static {
            try {
                (FIELD = Random.class.getDeclaredField("seed")).setAccessible(true);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
