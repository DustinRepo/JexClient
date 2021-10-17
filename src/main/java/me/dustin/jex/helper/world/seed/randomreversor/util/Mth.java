package me.dustin.jex.helper.world.seed.randomreversor.util;

import java.math.BigInteger;

public class Mth
{
    public static long pow2(final int bits) {
        return 1L << bits;
    }
    
    public static long mask(final int bits) {
        return pow2(bits) - 1L;
    }
    
    public static boolean isPowerOf2(final int n) {
        return (n & -n) == n;
    }
    
    public static double gcd(double a, double b) {
        while (b != 0.0) {
            final double temp;
            a = (temp = a % b);
            a = b;
            b = temp;
        }
        return a;
    }
    
    public static BigInteger lcm(final BigInteger a, final BigInteger b) {
        return a.multiply(b.divide(a.gcd(b)));
    }
    
    public static long modInverse(final long x, final int mod) {
        if ((x & 0x1L) == 0x0L) {
            throw new IllegalArgumentException("x is not coprime with the modulus");
        }
        long inv = 0L;
        long b = 1L;
        for (int i = 0; i < mod; ++i) {
            if ((b & 0x1L) == 0x1L) {
                inv |= 1L << i;
                b = b - x >> 1;
            }
            else {
                b >>= 1;
            }
        }
        return inv;
    }
}
