package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public final class BigFraction extends Object implements Comparable<BigFraction> {
  public static final BigFraction ZERO = new BigFraction(0L);
  public static final BigFraction ONE = new BigFraction(1L);
  public static final BigFraction HALF = new BigFraction(1L, 2L);
  public static final BigFraction MINUS_ONE = new BigFraction(-1L);

  private BigInteger ntor;
  private BigInteger dtor;

  public BigFraction(BigInteger numerator, BigInteger denominator) {
    if (denominator.signum() == 0) {
      throw new ArithmeticException("/ by zero");
    }
    this.ntor = numerator;
    this.dtor = denominator;
    simplify();
  }

  public BigFraction(long numerator, long denominator) { this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator)); }

  public BigFraction(BigInteger numerator) { this(numerator, BigInteger.ONE); }

  public BigFraction(long numerator) { this(numerator, 1L); }











  
  public static BigFraction parse(String str) {
    String[] parts = str.split("\\s*/\\s*", 2);
    try {
      if (parts.length == 1) {
        return new BigFraction(new BigInteger(parts[0]));
      }
      return new BigFraction(new BigInteger(parts[0]), new BigInteger(parts[1]));
    }
    catch (NumberFormatException|ArithmeticException e) {
      throw new NumberFormatException("For input string: " + str);
    } 
  }
  
  private void simplify() {
    if (this.ntor.signum() == 0) {
      this.dtor = BigInteger.ONE;
      
      return;
    } 
    if (this.dtor.signum() == -1) {
      this.ntor = this.ntor.negate();
      this.dtor = this.dtor.negate();
    } 
    
    BigInteger commonFactor = this.ntor.gcd(this.dtor);
    this.ntor = this.ntor.divide(commonFactor);
    this.dtor = this.dtor.divide(commonFactor);
  }






  
  public BigInteger getNumerator() { return this.ntor; }







  
  public BigInteger getDenominator() { return this.dtor; }








  
  public BigDecimal toBigDecimal(MathContext mc) { return (new BigDecimal(this.ntor)).divide(new BigDecimal(this.dtor), mc); }

  
  private static final MathContext TO_DOUBLE_CONTEXT = MathContext.DECIMAL64;





  
  public double toDouble() { return toBigDecimal(TO_DOUBLE_CONTEXT).doubleValue(); }








  
  public BigFraction add(BigFraction other) { return new BigFraction(this.ntor.multiply(other.dtor).add(other.ntor.multiply(this.dtor)), this.dtor.multiply(other.dtor)); }








  
  public BigFraction add(BigInteger other) { return new BigFraction(this.ntor.add(other.multiply(this.dtor)), this.dtor); }








  
  public BigFraction add(long other) { return add(BigInteger.valueOf(other)); }








  
  public BigFraction subtract(BigFraction other) { return new BigFraction(this.ntor.multiply(other.dtor).subtract(other.ntor.multiply(this.dtor)), this.dtor.multiply(other.dtor)); }








  
  public BigFraction subtract(BigInteger other) { return new BigFraction(this.ntor.subtract(other.multiply(this.dtor)), this.dtor); }








  
  public BigFraction subtract(long other) { return subtract(BigInteger.valueOf(other)); }








  
  public BigFraction multiply(BigFraction other) { return new BigFraction(this.ntor.multiply(other.ntor), this.dtor.multiply(other.dtor)); }








  
  public BigFraction multiply(BigInteger other) { return new BigFraction(this.ntor.multiply(other), this.dtor); }








  
  public BigFraction multiply(long other) { return multiply(BigInteger.valueOf(other)); }









  
  public BigFraction divide(BigFraction other) { return new BigFraction(this.ntor.multiply(other.dtor), this.dtor.multiply(other.ntor)); }









  
  public BigFraction divide(BigInteger other) { return new BigFraction(this.ntor, this.dtor.multiply(other)); }









  
  public BigFraction divide(long other) { return divide(BigInteger.valueOf(other)); }







  
  public BigFraction negate() { return new BigFraction(this.ntor.negate(), this.dtor); }








  
  public BigFraction reciprocal() { return new BigFraction(this.dtor, this.ntor); }






  
  public BigInteger floor() {
    if (this.dtor.equals(BigInteger.ONE))
      return this.ntor; 
    if (this.ntor.signum() == -1) {
      return this.ntor.divide(this.dtor).subtract(BigInteger.ONE);
    }
    return this.ntor.divide(this.dtor);
  }






  
  public BigInteger ceil() {
    if (this.dtor.equals(BigInteger.ONE))
      return this.ntor; 
    if (this.ntor.signum() == 1) {
      return this.ntor.divide(this.dtor).add(BigInteger.ONE);
    }
    return this.ntor.divide(this.dtor);
  }








  
  public BigInteger round() { return add(HALF).floor(); }







  
  public int signum() { return this.ntor.signum(); }







  
  public BigFraction abs() { return (this.ntor.signum() == -1) ? negate() : this; }



  
  public int compareTo(BigFraction other) { return this.ntor.multiply(other.dtor).compareTo(other.ntor.multiply(this.dtor)); }



  
  public int hashCode() { return this.ntor.hashCode() + 31 * this.dtor.hashCode(); }


  
  public boolean equals(Object other) {
    if (other == this) return true; 
    if (other == null || other.getClass() != BigFraction.class) return false; 
    BigFraction that = (BigFraction)other;
    return (this.ntor.equals(that.ntor) && this.dtor.equals(that.dtor));
  }

  
  public String toString() {
    if (this.dtor.equals(BigInteger.ONE)) {
      return this.ntor.toString();
    }
    return this.ntor + "/" + this.dtor;
  }
}
