package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import java.util.Arrays;


public final class BigVector {
  BigFraction[] numbers;
  private int dimension;
  int startPos;
  int step;
  
  public BigVector(int dimension) {
    this.startPos = 0;
    this.step = 1;
    this.dimension = dimension;
    this.numbers = new BigFraction[this.dimension];
    Arrays.fill(this.numbers, BigFraction.ZERO);
  }

  public BigVector(long... numbers) { this(toBigFractions(numbers)); }

  private static BigFraction[] toBigFractions(long[] numbers) {
    BigFraction[] fractions = new BigFraction[numbers.length];
    for (int i = 0; i < numbers.length; i++) {
      fractions[i] = new BigFraction(numbers[i]);
    }
    return fractions;
  }

  public BigVector(BigFraction... numbers) {
    this.startPos = 0;
    this.step = 1;
    this.dimension = numbers.length;
    this.numbers = numbers;
  }

  static BigVector createView(BigFraction[] array, int dimension, int startPos, int step) {
    BigVector vec = new BigVector(array);
    vec.dimension = dimension;
    vec.startPos = startPos;
    vec.step = step;
    return vec;
  }

  public int getDimension() { return this.dimension; }

  public BigFraction get(int i) {
    if (i < 0 || i >= this.dimension) {
      throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
    }
    return this.numbers[this.step * i + this.startPos];
  }

  public void set(int i, BigFraction value) {
    if (i < 0 || i >= this.dimension) {
      throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
    }
    this.numbers[this.step * i + this.startPos] = value;
  }

  public BigFraction magnitudeSq() {
    BigFraction magnitude = BigFraction.ZERO;
    
    for (int i = 0; i < getDimension(); i++) {
      magnitude = magnitude.add(get(i).multiply(get(i)));
    }
    
    return magnitude;
  }

  public boolean isZero() {
    for (int i = 0; i < getDimension(); i++) {
      if (get(i).signum() != 0) return false;
    
    } 
    return true;
  }

  public BigVector add(BigVector a) { return copy().addEquals(a); }

  public BigVector add(Object a) { return copy().addEquals((BigVector) a); }

  public BigVector subtract(BigVector a) { return copy().subtractEquals(a); }

  public BigVector multiply(BigFraction scalar) { return copy().multiplyEquals(scalar); }

  public BigVector multiply(BigMatrix m) {
    if (getDimension() != m.getRowCount()) {
      throw new IllegalArgumentException("Vector dimension should equal the number of matrix rows");
    }
    
    BigVector v = new BigVector(m.getColumnCount());
    
    for (int i = 0; i < v.getDimension(); i++) {
      v.set(i, dot(m.getColumn(i)));
    }
    
    return v;
  }

  public BigVector divide(BigFraction scalar) { return copy().divideEquals(scalar); }

  public BigVector swapNums(int i, int j) { return copy().swapNumsEquals(i, j); }

  public BigVector addEquals(BigVector a) {
    assertSameDimension(a);
    
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i).add(a.get(i)));
    }
    
    return this;
  }

  public BigVector subtractEquals(BigVector a) {
    assertSameDimension(a);
    
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i).subtract(a.get(i)));
    }
    
    return this;
  }

  public BigVector multiplyEquals(BigFraction scalar) {
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i).multiply(scalar));
    }
    
    return this;
  }

  public BigVector divideEquals(BigFraction scalar) {
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i).divide(scalar));
    }
    
    return this;
  }

  public BigVector swapNumsEquals(int i, int j) {
    BigFraction temp = get(i);
    set(i, get(j));
    set(j, temp);
    return this;
  }

  public BigFraction dot(BigVector v) {
    assertSameDimension(v);
    
    BigFraction dot = BigFraction.ZERO;
    
    for (int i = 0; i < getDimension(); i++) {
      dot = dot.add(get(i).multiply(v.get(i)));
    }
    
    return dot;
  }

  public BigFraction gramSchmidtCoefficient(BigVector v) { return dot(v).divide(v.magnitudeSq()); }

  public BigVector projectOnto(BigVector v) { return v.multiply(gramSchmidtCoefficient(v)); }

  public BigVector copy() {
    if (this.step == 1) {
      return new BigVector((BigFraction[])Arrays.copyOfRange(this.numbers, this.startPos, this.startPos + this.dimension));
    }
    
    BigVector v = new BigVector(getDimension());
    for (int i = 0; i < v.getDimension(); i++) {
      v.set(i, get(i));
    }
    return v;
  }
  
  private void assertSameDimension(BigVector other) {
    if (other.dimension != this.dimension) {
      throw new IllegalArgumentException("The other vector is not the same dimension");
    }
  }

  public int hashCode() {
    int h = 0;
    for (int i = 0; i < this.dimension; i++) {
      h = 31 * h + get(i).hashCode();
    }
    return h;
  }

  public boolean equals(Object other) {
    if (other == this) return true; 
    if (other == null || other.getClass() != BigVector.class) return false; 
    BigVector that = (BigVector)other;
    if (this.dimension != that.dimension) {
      return false;
    }
    for (int i = 0; i < this.dimension; i++) {
      if (!get(i).equals(that.get(i))) {
        return false;
      }
    } 
    return true;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    
    for (int i = 0; i < getDimension(); i++) {
      sb.append(get(i))
        .append((i == getDimension() - 1) ? "" : ", ");
    }
    
    return sb.append("}").toString();
  }

  public static BigVector fromString(String raw) {
    raw = raw.replaceAll("\\s+", "");
    
    String[] data = raw.split(",");
    BigVector v = new BigVector(data.length);
    
    for (int i = 0; i < data.length; i++) {
      v.set(i, BigFraction.parse(data[i]));
    }
    
    return v;
  }

  public static BigVector basis(int size, int i) { return basis(size, i, BigFraction.ONE); }


  public static BigVector basis(int size, int i, BigFraction scale) {
    BigVector vector = new BigVector(size);
    vector.set(i, scale);
    
    return vector;
  }
}
