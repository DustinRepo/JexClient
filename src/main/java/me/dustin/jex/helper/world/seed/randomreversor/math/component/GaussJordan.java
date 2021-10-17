package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class GaussJordan {
  @FunctionalInterface
  public static interface ReduceColumnPredicate {
    public static final ReduceColumnPredicate ALWAYS = (pivotColumn, pivotRows) -> true;
    
    boolean test(int param1Int, int[] param1ArrayOfInt);
  }
  
  private static void forAll(BigMatrix matrix, Collection<BigMatrix> others, Consumer<BigMatrix> action) {
    action.accept(matrix);
    others.forEach(action);
  }
  
  public static int[] reduce(BigMatrix matrix, Collection<BigMatrix> others, ReduceColumnPredicate reduceColumn) {
    int[] pivotRows = new int[matrix.getColumnCount()];
    Arrays.fill(pivotRows, -1);
    
    int row = 0;
    int pivotColumn = 0;
    
    while (row < matrix.getRowCount() && pivotColumn < matrix.getColumnCount()) {
      int pivotRow;
      
      for (pivotRow = row; pivotRow < matrix.getRowCount() && 
        matrix.get(pivotRow, pivotColumn).equals(BigFraction.ZERO); pivotRow++);

      if (pivotRow < matrix.getRowCount()) {
        int finalRow = row;
        int finalPivotRow = pivotRow;
        int finalPivotColumn = pivotColumn;
        
        BigFraction finalPivot = matrix.get(finalPivotRow, finalPivotColumn);
        
        forAll(matrix, others, m -> m.getRow(finalPivotRow).divideEquals(finalPivot));
        
        for (int i = 0; i < matrix.getRowCount(); i++) {
          if (i != finalPivotRow) {


            
            int finalI = i;
            BigFraction finalScale = matrix.get(i, finalPivotColumn);
            
            forAll(matrix, others, m -> m.getRow(finalI).subtractEquals(m.getRow(finalPivotRow).multiply(finalScale)));
          } 
        } 
        forAll(matrix, others, m -> m.swapRowsEquals(finalRow, finalPivotRow));
        pivotRows[finalPivotColumn] = finalRow;
        row++;
      } 
      
      do {
        pivotColumn++;
      } while (pivotColumn < matrix.getColumnCount() && !reduceColumn.test(pivotColumn, pivotRows));
    } 
    
    return pivotRows;
  }

  public static int[] reduce(BigMatrix matrix, BigMatrix other, ReduceColumnPredicate reduceColumn) { return reduce(matrix, Collections.singleton(other), reduceColumn); }

  public static int[] reduce(BigMatrix matrix, ReduceColumnPredicate reduceColumn) { return reduce(matrix, Collections.emptyList(), reduceColumn); }

  public static int[] reduce(BigMatrix matrix, Collection<BigMatrix> others) { return reduce(matrix, others, ReduceColumnPredicate.ALWAYS); }

  public static int[] reduce(BigMatrix matrix, BigMatrix other) { return reduce(matrix, Collections.singleton(other), ReduceColumnPredicate.ALWAYS); }

  public static int[] reduce(BigMatrix matrix) { return reduce(matrix, Collections.emptyList(), ReduceColumnPredicate.ALWAYS); }
}
