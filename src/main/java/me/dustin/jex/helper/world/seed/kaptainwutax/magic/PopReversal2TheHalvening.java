package me.dustin.jex.helper.world.seed.kaptainwutax.magic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class PopReversal2TheHalvening {

	private static long makeMask(int bits) {
		return (1L << bits) - 1;
	}

	private static final long mask16 = makeMask(16);
	private static final long mask32 = makeMask(32);
	private static final long m1 = 25214903917L; //the next 8 lines are constants for the lcg created by calling java lcg multiple times.
	//    private static final long addend1 = 11;
	private static final long m2 = 205749139540585L;
	private static final long addend2 = 277363943098L;
	//    private static final long m3 = 233752471717045L;
//    private static final long addend3 = 11718085204285L;
	private static final long m4 = 55986898099985L;
	private static final long addend4 = 49720483695876L;
	//private static ArrayList<Long> worldseeds;

	private static int countTrailingZeroes(long v) {
		int c;  // output: c will count v's trailing zero bits,
		// so if v is 1101000 (base 2), then c will be 3
		v = (v ^ (v - 1)) >> 1;  // Set v's trailing 0s to 1s and zero rest
		for (c = 0; v != 0; c++) {
			v >>>= 1;
		}
		return c;
	}

	private static long modInverse(long x, int mod) { //Fast method for modular inverse mod powers of 2
		long inv = 0;
		long b = 1;
		for (int i = 0; i < mod; i++) {
			if ((b & 1) == 1) {
				inv |= 1L << i;
				b = (b - x) >> 1;
			} else {
				b >>= 1;
			}
		}
		return inv;
	}

	private static long getChunkseed13Plus(long seed, int x, int z) {
		Random r = new Random(seed);
		long a = r.nextLong() | 1;
		long b = r.nextLong() | 1;
		return ((x * a + z * b) ^ seed) & ((1L << 48) - 1);
	}

	private static long makeSecondAddend13Plus(int x, long k, int z) {
		return ((((long) x) * (((int) (((m2 * ((k ^ m1) & mask32) + addend2) & ((1L << 48) - 1)) >>> 16)) | 1) +
				((long) z) * (((int) (((m4 * ((k ^ m1) & mask32) + addend4) & ((1L << 48) - 1)) >>> 16)) | 1)) >>> 16) & mask16;
	}

	private static ArrayList<Long> addWorldSeed13Plus(long firstAddend, int multTrailingZeroes, long firstMultInv, long c, long e, int x, int z, long chunkseed, ArrayList<Long> worldseeds) {
		if (countTrailingZeroes(firstAddend) >= multTrailingZeroes) { //Does there exist a set of 16 bits which work for bits 17-32
			long b = ((((firstMultInv * firstAddend) >>> multTrailingZeroes) ^ (m1 >> 16)) & ((1L << (16 - multTrailingZeroes)) - 1));

			long smallMask = ((1L << multTrailingZeroes) - 1);//These are longs but probably can be ints for nearly every chunk -
			// if you are porting my code for specific chunks check if long needed I guess - perhaps the algorithm will run no slower if you cap it at 16 bits
			//long

			//System.out.println(Long.toHexString(b));
			for (; b < (1L << 16); b += (1L << (16 - multTrailingZeroes))) { //if the previous multiplier had a power of 2 divisor, we get multiple solutions for b
				//System.out.println(b);
				long k = (b << 16) + c;
				long target2 = (k ^ e) >> 16; //now that we know b, we can undo more of the mask
				long secondAddend = makeSecondAddend13Plus(x, k, z);
				//System.out.println(secondAddend);
				if (countTrailingZeroes(target2 - secondAddend) >= multTrailingZeroes) { //Does there exist a set of 16 bits which work for bits 33-48
					long a = ((((firstMultInv * (target2 - secondAddend)) >>> multTrailingZeroes) ^ (m1 >> 32)) & ((1L << (16 - multTrailingZeroes)) - 1));
					for (; a < (1L << 16); a += (1L << (16 - multTrailingZeroes))) { //if the previous multiplier had a power of 2 divisor, we get multiple solutions for a
						if (getChunkseed13Plus((a << 32) + k, x, z) == chunkseed) { //lazy check if the test has succeeded
							//System.out.println(b+" "+c);
							worldseeds.add((a << 32) + k);
						}
					}
				}
			}
		}
		return worldseeds;
	}

	static ArrayList<Long> getSeedFromChunkseed13Plus(long chunkseed, int x, int z) {

		ArrayList<Long> worldseeds = new ArrayList<>();

		if (x == 0 && z == 0) {
			worldseeds.add(chunkseed);
			return worldseeds;
		}

		long c; //a is upper 16 bits, b middle 16 bits, c lower 16 bits of worldseed.
		long e = chunkseed & ((1L << 32) - 1); //The algorithm proceeds by solving for worldseed in 16 bit groups
		long f = chunkseed & (((1L << 16) - 1)); //as such, we need the 16 bit groups of chunkseed for later eqns.

        /*boolean xEven = ((x&1) == 0);
        boolean zEven = ((z&1) == 0);*/

		long firstMultiplier = (m2 * x + m4 * z) & mask16;
		int multTrailingZeroes = countTrailingZeroes(firstMultiplier); //TODO currently code blows up if this is 16, but you can use it to get bits of seed anyway if it is non zero
		long firstMultInv = modInverse(firstMultiplier >> multTrailingZeroes, 16);

		int xcount = countTrailingZeroes(x);
		int zcount = countTrailingZeroes(z);
		int totalCount = countTrailingZeroes(x | z);


		c = xcount == zcount ? chunkseed & ((1 << (xcount + 1)) - 1) : chunkseed & ((1 << (totalCount + 1)) - 1) ^ (1 << totalCount);

        /*//TODO We can recover more initial bits when x + z is divisible by a power of 2
        if (xEven ^ zEven) { //bottom bit of x*a + z*b is odd so we xor by 1 to get bottom bit of worldseed.
            c = (chunkseed & 1)^1;
        } else { //bottom bit of x*a + z*b is even so we xor by 0 to get bottom bit of worldseed.
            c = (chunkseed & 1);
        }*/

		for (; c < (1L << 16); c += (1 << (totalCount + 1))) { //iterate through all possible lower 16 bits of worldseed.
			//System.out.println(c);
			long target = (c ^ f) & mask16; //now that we've guessed 16 bits of worldseed we can undo the mask

			//We need to handle the four different cases of the effect the two |1s have on the seed
			long magic = x * ((m2 * ((c ^ m1) & mask16) + addend2) >>> 16) + z * ((m4 * ((c ^ m1) & mask16) + addend4) >>> 16);

			HashSet<Integer> possibleRoundingOffsets = new HashSet<>();
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < 2; j++)
					possibleRoundingOffsets.add(x * i + j * z);

			for (int i : possibleRoundingOffsets)
				addWorldSeed13Plus(target - ((magic + i) & mask16), multTrailingZeroes, firstMultInv, c, e, x, z, chunkseed, worldseeds); //case both nextLongs were odd

			//addWorldSeed13Plus(target - ((magic + x) & mask16), multTrailingZeroes, firstMultInv, c, e, x, z, chunkseed,worldseeds); //case where x nextLong even

			//addWorldSeed13Plus(target - ((magic + z) & mask16), multTrailingZeroes, firstMultInv, c, e, x, z, chunkseed,worldseeds); //case where z nextLong even

			//addWorldSeed13Plus(target - ((magic + x + z) & mask16), multTrailingZeroes, firstMultInv, c, e, x, z, chunkseed,worldseeds); //case where both nextLongs even
		}

		return worldseeds;
	}

	private static long getChunkseedPre13(long seed, int x, int z) {
		Random r = new Random(seed);
		long a = r.nextLong() / 2 * 2 + 1;
		long b = r.nextLong() / 2 * 2 + 1;
		return ((x * a + z * b) ^ seed) & ((1L << 48) - 1);
	}

	private static long getPartialAddendPre13(long partialSeed, int x, int z, int bits) {
		long mask = makeMask(bits);
		return ((long) x) * (((int) (((m2 * ((partialSeed ^ m1) & mask) + addend2) & ((1L << 48) - 1)) >>> 16)) / 2 * 2 + 1) +
				((long) z) * (((int) (((m4 * ((partialSeed ^ m1) & mask) + addend4) & ((1L << 48) - 1)) >>> 16)) / 2 * 2 + 1);
	}

	private static ArrayList<Long> addWorldSeedPre13(long firstAddend, int multTrailingZeroes, long firstMultInv, long c, int x, int z, long chunkseed, ArrayList<Long> worldseeds) {
		long bottom32BitsChunkseed = chunkseed & mask32;

		if (countTrailingZeroes(firstAddend) >= multTrailingZeroes) { //Does there exist a set of 16 bits which work for bits 17-32
			long b = ((((firstMultInv * firstAddend) >>> multTrailingZeroes) ^ (m1 >> 16)) & makeMask(16 - multTrailingZeroes));
			if (multTrailingZeroes != 0) {
				long smallMask = makeMask(multTrailingZeroes);//These are longs but probably can be ints for nearly every chunk -
				long smallMultInverse = smallMask & firstMultInv;
				long target = (((b ^ (bottom32BitsChunkseed >>> 16)) & smallMask) -
						(getPartialAddendPre13((b << 16) + c, x, z, 32 - multTrailingZeroes) >>> 16)) & smallMask;
				b += (((target * smallMultInverse) ^ (m1 >> (32 - multTrailingZeroes))) & smallMask) << (16 - multTrailingZeroes);
			}
			long bottom32BitsSeed = (b << 16) + c;
			long target2 = (bottom32BitsSeed ^ bottom32BitsChunkseed) >> 16; //now that we know b, we can undo more of the mask
			long secondAddend = (getPartialAddendPre13(bottom32BitsSeed, x, z, 32) >>> 16);
			secondAddend &= mask16;
			long topBits = ((((firstMultInv * (target2 - secondAddend)) >>> multTrailingZeroes) ^ (m1 >> 32)) & makeMask(16 - multTrailingZeroes));
			for (; topBits < (1L << 16); topBits += (1L << (16 - multTrailingZeroes))) { //if the previous multiplier had a power of 2 divisor, we get multiple solutions for a
				if ((getChunkseedPre13((topBits << 32) + bottom32BitsSeed, x, z)) == (chunkseed)) { //lazy check if the test has succeeded
					worldseeds.add((topBits << 32) + bottom32BitsSeed);
				}
			}
		}
		return worldseeds;
	}

	public static ArrayList<Long> getSeedFromChunkseedPre13(long chunkseed, int x, int z) {

		ArrayList<Long> worldseeds = new ArrayList<>();

		if (x == 0 && z == 0) {
			worldseeds.add(chunkseed);
			return worldseeds;
		}

		long c; //a is upper 16 bits, b middle 16 bits, c lower 16 bits of worldseed.
		long e = chunkseed & ((1L << 32) - 1); //The algorithm proceeds by solving for worldseed in 16 bit groups
		long f = chunkseed & (((1L << 16) - 1)); //as such, we need the 16 bit groups of chunkseed for later eqns.

		long firstMultiplier = (m2 * x + m4 * z) & mask16;
		int multTrailingZeroes = countTrailingZeroes(firstMultiplier); //TODO currently code blows up if this is 8, but you can use it to get bits of seed anyway if it is non zero and you are reversing seeds in bulk
		long firstMultInv = modInverse(firstMultiplier >> multTrailingZeroes, 16);

		int xcount = countTrailingZeroes(x);
		int zcount = countTrailingZeroes(z);
		int totalCount = countTrailingZeroes(x | z);

		c = xcount == zcount ? chunkseed & ((1 << (xcount + 1)) - 1) : chunkseed & ((1 << (totalCount + 1)) - 1) ^ (1 << totalCount);
		for (; c < (1L << 16); c += (1 << (totalCount + 1))) { //iterate through all possible lower 16 bits of worldseed.
			//System.out.println(c);
			long target = (c ^ f) & mask16; //now that we've guessed 16 bits of worldseed we can undo the mask
			//We need to handle the four different cases of the effect the two |1s have on the seed
			long magic = x * ((m2 * ((c ^ m1) & mask16) + addend2) >>> 16) + z * ((m4 * ((c ^ m1) & mask16) + addend4) >>> 16);

			//TODO Many of these checks can be unneeded if there is collision among the firstAddend values. For example if 2x+z = x+2z you only need to check one of them.
			//TODO Investigate possibility algorithm may return same seed multiple times. Think it shouldn't occur if you avoid collision as above, but perhaps rarely?
			HashSet<Integer> possibleRoundingOffsets = new HashSet<>();
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					possibleRoundingOffsets.add(x * i + j * z);

			for (int i : possibleRoundingOffsets)
				addWorldSeedPre13(target - ((magic + i) & mask16), multTrailingZeroes, firstMultInv, c, x, z, chunkseed, worldseeds); //case both nextLongs were odd
		}

		return worldseeds;
	}

}