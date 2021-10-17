package me.dustin.jex.helper.world.seed.randomreversor.util;

import java.util.Objects;

public class LCG
{
    public static final LCG JAVA;
    public final long multiplier;
    public final long addend;
    public final long modulus;
    private final boolean canMask;
    
    public LCG(final long multiplier, final long addend, final long modulus) {
        this.multiplier = multiplier;
        this.addend = addend;
        this.modulus = modulus;
        this.canMask = ((this.modulus & -this.modulus) == this.modulus);
    }
    
    public long nextSeed(final long seed) {
        return this.mod(seed * this.multiplier + this.addend);
    }
    
    public LCG combine(final long steps) {
        long multiplier = 1L;
        long addend = 0L;
        long intermediateMultiplier = this.multiplier;
        long intermediateAddend = this.addend;
        for (long k = steps; k != 0L; k >>>= 1) {
            if ((k & 0x1L) != 0x0L) {
                multiplier *= intermediateMultiplier;
                addend = intermediateMultiplier * addend + intermediateAddend;
            }
            intermediateAddend *= intermediateMultiplier + 1L;
            intermediateMultiplier *= intermediateMultiplier;
        }
        multiplier = this.mod(multiplier);
        addend = this.mod(addend);
        return new LCG(multiplier, addend, this.modulus);
    }
    
    public LCG invert() {
        return this.combine(-1L);
    }
    
    public long mod(final long n) {
        if (this.canMask) {
            return n & this.modulus - 1L;
        }
        return n % this.modulus;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LCG)) {
            return false;
        }
        final LCG lcg = (LCG)obj;
        return this.multiplier == lcg.multiplier && this.addend == lcg.addend && this.modulus == lcg.modulus;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.multiplier, this.addend, this.modulus);
    }
    
    @Override
    public String toString() {
        return "LCG{multiplier=" + this.multiplier + ", addend=" + this.addend + ", modulo=" + this.modulus + '}';
    }
    
    static {
        JAVA = new LCG(25214903917L, 11L, 281474976710656L);
    }
}
