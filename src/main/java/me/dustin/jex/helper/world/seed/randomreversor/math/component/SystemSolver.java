package me.dustin.jex.helper.world.seed.randomreversor.math.component;

public class SystemSolver {
  public static Result solve(Matrix base, Matrix extra, Phase phase) {
    AugmentedMatrix am = new AugmentedMatrix(base.copy(), extra.copy());
    
    for (int x = 0; x < am.getBase().getColumnCount(); x++) {
      Vector v = am.getBase().getRow(x);
      
      if (v.get(x) != 0.0D && v.get(x) != 1.0D) {
        am.divideRow(x, v.get(x));
      } else if (v.get(x) == 0.0D) {
        continue;
      } 
      
      for (int y = x + 1; y < am.getBase().getRowCount(); y++) {
        if (am.getBase().get(y, x) != 0.0D)
          am.subtractScaledRow(y, am.getBase().get(y, x), x); 
      } 
      continue;
    } 
    if (phase == Phase.ROW_ECHELON) return new Result(am);
    
    for (int j = 0; j < am.getBase().getColumnCount(); j++) {
      for (int i = 0; i < j; i++) {
        Vector v = am.getBase().getRow(i);
        Vector s = am.getBase().getRow(j);
        if (v.get(j) != 0.0D && s.get(j) == 1.0D) {
          am.subtractScaledRow(i, v.get(j), j);
        }
      } 
    } 
    return new Result(am);
  }
  
  public static BigResult solve(BigMatrix base, BigMatrix extra, Phase phase) {
    BigAugmentedMatrix am = new BigAugmentedMatrix(base.copy(), extra.copy());
    
    for (int x = 0; x < am.getBase().getColumnCount(); x++) {
      BigVector v = am.getBase().getRow(x);
      
      if (v.get(x).signum() != 0 && !v.get(x).equals(BigFraction.ONE)) {
        am.divideRow(x, v.get(x));
      } else if (v.get(x).signum() == 0) {
        continue;
      } 
      
      for (int y = x + 1; y < am.getBase().getRowCount(); y++) {
        if (am.getBase().get(y, x).signum() != 0)
          am.subtractScaledRow(y, am.getBase().get(y, x), x); 
      } 
      continue;
    } 
    if (phase == Phase.ROW_ECHELON) return new BigResult(am);
    
    for (int j = 0; j < am.getBase().getColumnCount(); j++) {
      for (int i = 0; i < j; i++) {
        BigVector v = am.getBase().getRow(i);
        BigVector s = am.getBase().getRow(j);
        if (v.get(j).signum() != 0) {
          am.subtractScaledRow(i, v.get(j), j);
        }
      } 
    } 
    return new BigResult(am);
  }
  
  public enum Phase {
    ROW_ECHELON, BASIS;
  }
  
  public static class Result {
    public Matrix result;
    public AugmentedMatrix remainder;
    public Type type;
    
    public Result(AugmentedMatrix am) {
      this.result = new Matrix(am.getExtra().getRowCount(), am.getExtra().getColumnCount());
      this.remainder = am;
      this.type = Type.ONE_SOLUTION;
      
      for (int i = 0; i < am.getBase().getRowCount(); i++) {
        Vector baseV = am.getBase().getRow(i);
        Vector extraV = am.getExtra().getRow(i);
        boolean isBaseZero = baseV.isZero();
        boolean isExtraZero = extraV.isZero();
        
        if (!isBaseZero) {
          
          this.result.setRow(i, extraV);
          updateType(Type.ONE_SOLUTION);
        } else if (isExtraZero) {
          updateType(Type.INFINITE_SOLUTIONS);
        } else {
          updateType(Type.NO_SOLUTIONS);
        } 
      } 
    }
    
    public void updateType(Type type) {
      if (type.ordinal() > this.type.ordinal()) {
        this.type = type;
      }
    }

    
    public String toString() {
      return "This system has " + this.type + ".\n\nResult: \n" + this.result
        .toPrettyString() + "\n\nRemainder: \n" + this.remainder
        .toString();
    }
    
    public enum Type {
      ONE_SOLUTION, NO_SOLUTIONS, INFINITE_SOLUTIONS;
    }
  }
  
  public static class BigResult {
    public BigMatrix result;
    public BigAugmentedMatrix remainder;
    public Type type;
    
    public BigResult(BigAugmentedMatrix am) {
      this.result = new BigMatrix(am.getExtra().getRowCount(), am.getExtra().getColumnCount());
      this.remainder = am;
      this.type = Type.ONE_SOLUTION;
      
      for (int i = 0; i < am.getBase().getRowCount(); i++) {
        BigVector baseV = am.getBase().getRow(i);
        BigVector extraV = am.getExtra().getRow(i);
        boolean isBaseZero = baseV.isZero();
        boolean isExtraZero = extraV.isZero();
        
        if (!isBaseZero) {
          
          this.result.setRow(i, extraV);
          updateType(Type.ONE_SOLUTION);
        } else if (isExtraZero) {
          updateType(Type.INFINITE_SOLUTIONS);
        } else {
          updateType(Type.NO_SOLUTIONS);
        } 
      } 
    }
    
    public void updateType(Type type) {
      if (type.ordinal() > this.type.ordinal()) {
        this.type = type;
      }
    }

    
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("This system has ").append(this.type).append(".\n\n");
      sb.append("Result: \n").append(this.result.toPrettyString()).append("\n\n");
      sb.append("Remainder: \n").append(this.remainder.toString());
      return sb.toString();
    }
    
    public enum Type {
      ONE_SOLUTION, NO_SOLUTIONS, INFINITE_SOLUTIONS;
    }
  }
}
