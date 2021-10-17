package me.dustin.jex.helper.world.seed.kaptainwutax.util;


import java.util.Random;

public class Rand implements Cloneable {

	public static final LCG JAVA_LCG = new LCG(0x5DEECE66DL, 0xBL, 1L << 48);

	private long seed;
	private boolean haveNextNextGaussian = false;
	private double nextNextGaussian;

	public Rand(long seed) {
		this.setSeed(seed, true);
	}

	public Rand(long seed, boolean scramble) {
		this.setSeed(seed, scramble);
	}

	public long getSeed() {
		return this.seed;
	}

	public void setSeed(long seed, boolean scramble) {
		this.seed = seed ^ (scramble ? JAVA_LCG.multiplier : 0L);
		this.seed &= JAVA_LCG.modulo - 1;
		this.haveNextNextGaussian = false;
	}

	public int next(int bits) {
		this.seed = JAVA_LCG.nextSeed(this.seed);
		return (int) (this.seed >>> (48 - bits));
	}

	public void advance(int calls) {
		this.advance(JAVA_LCG.combine(calls));
	}

	public void advance(LCG skip) {
		this.seed = skip.nextSeed(this.seed);
	}

	public boolean nextBoolean() {
		return this.next(1) == 1;
	}

	public int nextInt() {
		return this.next(32);
	}

	public int nextInt(int bound) {
		if (bound <= 0) {
			throw new IllegalArgumentException("bound must be positive");
		}

		if ((bound & -bound) == bound) {
			return (int) ((bound * (long) this.next(31)) >> 31);
		}

		int bits, value;

		do {
			bits = this.next(31);
			value = bits % bound;
		} while (bits - value + (bound - 1) < 0);

		return value;
	}

	public float nextFloat() {
		return this.next(24) / ((float) (1 << 24));
	}

	public long nextLong() {
		return ((long) (this.next(32)) << 32) + this.next(32);
	}

	public double nextDouble() {
		return (((long) this.next(26) << 27) + this.next(27)) / (double) (1L << 53);
	}

	public double nextGaussian() {
		if (this.haveNextNextGaussian) {
			this.haveNextNextGaussian = false;
			return this.nextNextGaussian;
		}

		double v1, v2, s;
		do {
			v1 = 2 * nextDouble() - 1;
			v2 = 2 * nextDouble() - 1;
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
		this.nextNextGaussian = v2 * multiplier;
		haveNextNextGaussian = true;
		return v1 * multiplier;
	}

	public Random toRandom() {
		return new Random(this.seed ^ JAVA_LCG.multiplier);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Rand)) return false;
		Rand rand = (Rand) obj;
		return rand.getSeed() == this.getSeed();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Rand{");
		sb.append("seed=").append(seed);
		sb.append('}');
		return sb.toString();
	}

	@Override
	protected Object clone() {
		Rand rand = new Rand(this.getSeed(), false);
		return rand;
	}

}

