package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

public final class Vector {
  double[] numbers;
  private int dimension;
  int startPos;
  int step;
  
  public Vector(int dimension) {
    this.startPos = 0;
    this.step = 1;

    this.dimension = dimension;
    this.numbers = new double[this.dimension];
  }

  public Vector(double... numbers) {
    this.startPos = 0;
    this.step = 1;
    this.numbers = numbers;
    this.dimension = this.numbers.length;
  }





  
  public Vector(int dimension, IntToDoubleFunction generator) {
    this.startPos = 0;
    this.step = 1;
    this.dimension = dimension;
    this.numbers = new double[this.dimension];
    Arrays.setAll(this.numbers, generator);
  }

  
  static Vector createView(double[] array, int dimension, int startPos, int step) {
    Vector vec = new Vector(array);
    vec.dimension = dimension;
    vec.startPos = startPos;
    vec.step = step;
    return vec;
  }






  
  public int getDimension() { return this.dimension; }








  
  public double get(int i) {
    if (i < 0 || i >= this.dimension) {
      throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
    }
    return this.numbers[this.step * i + this.startPos];
  }







  
  public void set(int i, double value) {
    if (i < 0 || i >= this.dimension) {
      throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
    }
    this.numbers[this.step * i + this.startPos] = value;
  }








  
  public double magnitude() { return Math.sqrt(magnitudeSq()); }






  
  public double magnitudeSq() {
    double magnitude = 0.0D;
    
    for (int i = 0; i < getDimension(); i++) {
      magnitude += get(i) * get(i);
    }
    
    return magnitude;
  }





  
  public boolean isZero() {
    for (int i = 0; i < getDimension(); i++) {
      if (get(i) != 0.0D) return false;
    
    } 
    return true;
  }









  
  public Vector add(Vector a) { return copy().addEquals(a); }










  
  public Vector subtract(Vector a) { return copy().subtractEquals(a); }








  
  public Vector multiply(double scalar) { return copy().multiplyEquals(scalar); }










  
  public Vector multiply(Matrix m) {
    if (getDimension() != m.getRowCount()) {
      throw new IllegalArgumentException("Vector dimension should equal the number of matrix rows");
    }
    
    Vector v = new Vector(m.getColumnCount());
    
    for (int i = 0; i < v.getDimension(); i++) {
      v.set(i, dot(m.getColumn(i)));
    }
    
    return v;
  }







  
  public Vector divide(double scalar) { return copy().divideEquals(scalar); }










  
  public Vector swapNums(int i, int j) { return copy().swapNumsEquals(i, j); }









  
  public Vector addEquals(Vector a) {
    assertSameDimension(a);
    
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i) + a.get(i));
    }
    
    return this;
  }








  
  public Vector subtractEquals(Vector a) {
    assertSameDimension(a);
    
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i) - a.get(i));
    }
    
    return this;
  }






  
  public Vector multiplyEquals(double scalar) {
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i) * scalar);
    }
    
    return this;
  }






  
  public Vector divideEquals(double scalar) {
    for (int i = 0; i < getDimension(); i++) {
      set(i, get(i) / scalar);
    }
    
    return this;
  }








  
  public Vector swapNumsEquals(int i, int j) {
    double temp = get(i);
    set(i, get(j));
    set(j, temp);
    return this;
  }








  
  public double dot(Vector v) {
    assertSameDimension(v);
    
    double dot = 0.0D;
    
    for (int i = 0; i < getDimension(); i++) {
      dot += get(i) * v.get(i);
    }
    
    return dot;
  }












  
  public double gramSchmidtCoefficient(Vector v) { return dot(v) / v.magnitudeSq(); }











  
  public Vector projectOnto(Vector v) { return v.multiply(gramSchmidtCoefficient(v)); }






  
  public Vector copy() {
    if (this.step == 1) {
      return new Vector(Arrays.copyOfRange(this.numbers, this.startPos, this.startPos + this.dimension));
    }
    Vector dest = new Vector(getDimension());
    for (int i = 0; i < dest.getDimension(); i++) {
      dest.set(i, get(i));
    }
    return dest;
  }

  
  private void assertSameDimension(Vector other) {
    if (other.dimension != this.dimension) {
      throw new IllegalArgumentException("The other vector is not the same dimension");
    }
  }








  
  public boolean equals(Vector other, double tolerance) {
    if (other.dimension != this.dimension) {
      return false;
    }
    for (int i = 0; i < this.dimension; i++) {
      if (Math.abs(other.get(i) - get(i)) > tolerance) {
        return false;
      }
    } 
    return true;
  }

  
  public int hashCode() {
    int h = 0;
    for (int i = 0; i < this.dimension; i++) {
      h = 31 * h + Double.hashCode(get(i));
    }
    return h;
  }

  
  public boolean equals(Object other) {
    if (other == this) return true; 
    if (other == null || other.getClass() != Vector.class) return false; 
    Vector that = (Vector)other;
    return equals(that, 0.0D);
  }

  
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    
    for (int i = 0; i < getDimension(); i++) {
      sb.append(get(i)).append((i == getDimension() - 1) ? "" : ", ");
    }
    
    return sb.append("}").toString();
  }








  
  public static Vector fromString(String raw) {
    raw = raw.replaceAll("\\s+", "");
    
    String[] data = raw.split(",");
    Vector v = new Vector(data.length);
    
    for (int i = 0; i < data.length; i++) {
      v.set(i, Double.parseDouble(data[i]));
    }
    
    return v;
  }






  
  public static Vector fromBigVector(BigVector v) {
    Vector p = new Vector(v.getDimension());
    
    for (int i = 0; i < p.getDimension(); i++) {
      p.set(i, v.get(i).toDouble());
    }
    
    return p;
  }
}
