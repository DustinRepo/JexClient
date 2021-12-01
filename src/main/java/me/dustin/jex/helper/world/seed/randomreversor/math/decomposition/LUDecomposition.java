package me.dustin.jex.helper.world.seed.randomreversor.math.decomposition;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.*;
import java.util.regex.*;

public class LUDecomposition
{
  public static Result decompose(final Matrix matrix) {
    if (!matrix.isSquare()) {
      throw new UnsupportedOperationException("Matrix is not square");
    }
    final Matrix m = matrix.copy();
    final int size = m.getRowCount();
    final Vector p = new Vector(size);
    final Matrix inv = Matrix.identityMatrix(size);
    int swaps = 0;
    for (int i = 0; i < size; ++i) {
      int pivot = -1;
      double beegestNumbor = 0.0;
      for (int row = i; row < size; ++row) {
        final double d = Math.abs(m.get(row, i));
        if (d > beegestNumbor) {
          beegestNumbor = d;
          pivot = row;
        }
      }
      if (pivot == -1) {
        throw new IllegalStateException("Matrix is singular");
      }
      p.set(i, pivot);
      inv.swapRowsEquals(i, pivot);
      if (pivot != i) {
        m.swapRowsEquals(i, pivot);
        ++swaps;
      }
      for (int row = i + 1; row < size; ++row) {
        m.set(row, i, m.get(row, i) / m.get(i, i));
      }
      for (int row = i + 1; row < size; ++row) {
        for (int col = i + 1; col < size; ++col) {
          m.set(row, col, m.get(row, col) - m.get(row, i) * m.get(i, col));
        }
      }
    }
    double det = 1.0;
    for (int j = 0; j < size; ++j) {
      det *= m.get(j, j);
    }
    det *= (((swaps & 0x1) == 0x0) ? 1.0 : -1.0);
    for (int dcol = 0; dcol < size; ++dcol) {
      for (int row2 = 0; row2 < size; ++row2) {
        for (int col2 = 0; col2 < row2; ++col2) {
          inv.set(row2, dcol, inv.get(row2, dcol) - m.get(row2, col2) * inv.get(col2, dcol));
        }
      }
    }
    for (int dcol = 0; dcol < size; ++dcol) {
      for (int row2 = size - 1; row2 >= 0; --row2) {
        for (int col2 = size - 1; col2 > row2; --col2) {
          inv.set(row2, dcol, inv.get(row2, dcol) - m.get(row2, col2) * inv.get(col2, dcol));
        }
        inv.set(row2, dcol, inv.get(row2, dcol) / m.get(row2, row2));
      }
    }
    return new Result(m, p, det, inv);
  }

  public static BigResult decompose(final BigMatrix matrix) {
    if (!matrix.isSquare()) {
      throw new UnsupportedOperationException("Matrix is not square");
    }
    final BigMatrix m = matrix.copy();
    final int size = m.getRowCount();
    final BigVector p = new BigVector(size);
    final BigMatrix inv = BigMatrix.identityMatrix(size);
    int swaps = 0;
    for (int i = 0; i < size; ++i) {
      int pivot = -1;
      BigFraction beegestNumbor = BigFraction.ZERO;
      for (int row = i; row < size; ++row) {
        final BigFraction d = m.get(row, i).abs();
        if (d.compareTo(beegestNumbor) > 0) {
          beegestNumbor = d;
          pivot = row;
        }
      }
      if (pivot == -1) {
        throw new IllegalStateException("Matrix is singular");
      }
      p.set(i, new BigFraction(pivot));
      inv.swapRowsEquals(i, pivot);
      if (pivot != i) {
        m.swapRowsEquals(i, pivot);
        ++swaps;
      }
      for (int row = i + 1; row < size; ++row) {
        m.set(row, i, m.get(row, i).divide(m.get(i, i)));
      }
      for (int row = i + 1; row < size; ++row) {
        for (int col = i + 1; col < size; ++col) {
          m.set(row, col, m.get(row, col).subtract(m.get(row, i).multiply(m.get(i, col))));
        }
      }
    }
    BigFraction det = BigFraction.ONE;
    for (int j = 0; j < size; ++j) {
      det = det.multiply(m.get(j, j));
    }
    if ((swaps & 0x1) != 0x0) {
      det.negate();
    }
    for (int dcol = 0; dcol < size; ++dcol) {
      for (int row2 = 0; row2 < size; ++row2) {
        for (int col2 = 0; col2 < row2; ++col2) {
          inv.set(row2, dcol, inv.get(row2, dcol).subtract(m.get(row2, col2).multiply(inv.get(col2, dcol))));
        }
      }
    }
    for (int dcol = 0; dcol < size; ++dcol) {
      for (int row2 = size - 1; row2 >= 0; --row2) {
        for (int col2 = size - 1; col2 > row2; --col2) {
          inv.set(row2, dcol, inv.get(row2, dcol).subtract(m.get(row2, col2).multiply(inv.get(col2, dcol))));
        }
        inv.set(row2, dcol, inv.get(row2, dcol).divide(m.get(row2, row2)));
      }
    }
    return new BigResult(m, p, det, inv);
  }

  public static final class Result
  {
    private final int size;
    private final Matrix P;
    private final Matrix L;
    private final Matrix U;
    private final double det;
    private final Matrix inv;

    private Result(final Matrix lu, final Vector p, final double det, final Matrix inv) {
      this.size = lu.getRowCount();
      this.L = new Matrix(this.size, this.size, (row, col) -> {
        if (row > col) {
          return lu.get(row, col);
        }
        else if (row == col) {
          return 1.0;
        }
        else {
          return 0.0;
        }
      });
      this.U = new Matrix(this.size, this.size, (row, col) -> {
        if (row <= col) {
          return lu.get(row, col);
        }
        else {
          return 0.0;
        }
      });
      this.P = Matrix.identityMatrix(this.size);
      for (int i = 0; i < this.size; ++i) {
        this.P.swapRowsEquals(i, (int)p.get(i));
      }
      this.det = det;
      this.inv = inv;
    }

    public int getMatrixSize() {
      return this.size;
    }

    public Matrix getP() {
      return this.P;
    }

    public Matrix getL() {
      return this.L;
    }

    public Matrix getU() {
      return this.U;
    }

    public double getDet() {
      return this.det;
    }

    public Matrix inverse() {
      return this.inv;
    }

    public String toPrettyString() {
      final StringBuilder sb = new StringBuilder();
      final String[] uStuff = this.U.toPrettyString().split(Pattern.quote("\n"));
      final String[] lStuff = this.L.toPrettyString().split(Pattern.quote("\n"));
      final String[] pStuff = this.P.toPrettyString().split(Pattern.quote("\n"));
      for (int i = 0; i < lStuff.length; ++i) {
        sb.append(lStuff[i]).append("  ").append(uStuff[i]).append("  ").append(pStuff[i]);
        if (i != lStuff.length - 1) {
          sb.append("\n");
        }
      }
      return sb.toString();
    }

    @Override
    public String toString() {
      return this.P + " | " + this.L + " | " + this.U;
    }
  }

  public static final class BigResult
  {
    private final int size;
    private final BigMatrix P;
    private final BigMatrix L;
    private final BigMatrix U;
    private final BigFraction det;
    private final BigMatrix inv;

    private BigResult(final BigMatrix lu, final BigVector p, final BigFraction det, final BigMatrix inv) {
      this.size = lu.getRowCount();
      this.L = new BigMatrix(this.size, this.size, (row, col) -> {
        if (row > col) {
          return lu.get(row, col);
        }
        else if (row == col) {
          return BigFraction.ONE;
        }
        else {
          return BigFraction.ZERO;
        }
      });
      this.U = new BigMatrix(this.size, this.size, (row, col) -> {
        if (row <= col) {
          return lu.get(row, col);
        }
        else {
          return BigFraction.ZERO;
        }
      });
      this.P = BigMatrix.identityMatrix(this.size);
      for (int i = 0; i < this.size; ++i) {
        this.P.swapRowsEquals(i, p.get(i).getNumerator().intValue());
      }
      this.det = det;
      this.inv = inv;
    }

    public int getMatrixSize() {
      return this.size;
    }

    public BigMatrix getP() {
      return this.P;
    }

    public BigMatrix getL() {
      return this.L;
    }

    public BigMatrix getU() {
      return this.U;
    }

    public BigFraction getDet() {
      return this.det;
    }

    public BigMatrix inverse() {
      return this.inv;
    }

    public String toPrettyString() {
      final StringBuilder sb = new StringBuilder();
      final String[] uStuff = this.U.toPrettyString().split(Pattern.quote("\n"));
      final String[] lStuff = this.L.toPrettyString().split(Pattern.quote("\n"));
      final String[] pStuff = this.P.toPrettyString().split(Pattern.quote("\n"));
      for (int i = 0; i < lStuff.length; ++i) {
        sb.append(lStuff[i]).append("  ").append(uStuff[i]).append("  ").append(pStuff[i]);
        if (i != lStuff.length - 1) {
          sb.append("\n");
        }
      }
      return sb.toString();
    }

    @Override
    public String toString() {
      return this.P + " | " + this.L + " | " + this.U;
    }
  }
}
