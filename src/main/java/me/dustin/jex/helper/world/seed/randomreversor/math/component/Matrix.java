package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import me.dustin.jex.helper.world.seed.randomreversor.math.decomposition.LUDecomposition;
import me.dustin.jex.helper.world.seed.randomreversor.util.StringUtils;

public final class Matrix {
  private double[] numbers;
  private int rowCount;
  private int columnCount;
  private int startIndex;
  private int underlyingColumnCount;
  
  public Matrix(int rowCount, int columnCount) {
    this.startIndex = 0;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.underlyingColumnCount = columnCount;
    
    if (rowCount <= 0 || columnCount <= 0) {
      throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
    }
    
    this.numbers = new double[rowCount * columnCount];
  }

  public Matrix(int rowCount, int columnCount, DataProvider gen) {
    this(rowCount, columnCount);
    
    for (int row = 0; row < this.rowCount; row++) {
      for (int col = 0; col < this.columnCount; col++)
        set(row, col, gen.getValue(row, col)); 
    } 
  }
  
  private Matrix(int rowCount, int columnCount, double[] numbers, int startIndex, int underlyingColumnCount) {
    this.startIndex = 0;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.numbers = numbers;
    this.startIndex = startIndex;
    this.underlyingColumnCount = underlyingColumnCount;
  }

  public int getRowCount() { return this.rowCount; }

  public int getColumnCount() { return this.columnCount; }

  public boolean isSquare() { return (this.rowCount == this.columnCount); }

  public double get(int row, int col) {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
    }
    return this.numbers[this.startIndex + col + this.underlyingColumnCount * row];
  }

  public void set(int row, int col, double value) {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
    }
    this.numbers[this.startIndex + col + this.underlyingColumnCount * row] = value;
  }

  public Vector getRow(int rowIndex) {
    if (rowIndex < 0 || rowIndex >= this.rowCount) {
      throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
    }
    return Vector.createView(this.numbers, this.columnCount, this.startIndex + rowIndex * this.underlyingColumnCount, 1);
  }

  public Vector getColumn(int columnIndex) {
    if (columnIndex < 0 || columnIndex >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
    }
    return Vector.createView(this.numbers, this.rowCount, this.startIndex + columnIndex, this.underlyingColumnCount);
  }

  public void setRow(int rowIndex, Vector newRow) {
    if (newRow.getDimension() != this.columnCount) {
      throw new IllegalArgumentException("Invalid vector dimension, expected " + this.columnCount + ", got " + newRow.getDimension());
    }
    if (rowIndex < 0 || rowIndex >= this.rowCount) {
      throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
    }
    
    if (newRow.step == 1 && this.columnCount == this.underlyingColumnCount) {
      System.arraycopy(newRow.numbers, newRow.startPos, this.numbers, this.startIndex + rowIndex * this.columnCount, this.columnCount);
    } else {
      for (int i = 0; i < this.columnCount; i++) {
        set(rowIndex, i, newRow.get(i));
      }
    } 
  }

  public void setColumn(int columnIndex, Vector newColumn) {
    if (newColumn.getDimension() != this.rowCount) {
      throw new IllegalArgumentException("Invalid vector dimension, expected " + this.rowCount + ", got " + newColumn.getDimension());
    }
    if (columnIndex < 0 || columnIndex >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
    }
    
    for (int i = 0; i < this.rowCount; i++) {
      set(i, columnIndex, newColumn.get(i));
    }
  }

  public Matrix submatrix(int startRow, int startColumn, int rowCount, int columnCount) {
    if (startRow < 0 || startColumn < 0 || rowCount <= 0 || columnCount <= 0 || startRow + rowCount >= this.rowCount || startColumn + columnCount >= this.columnCount) {
      throw new IllegalArgumentException(String.format("Illegal submatrix start (%d, %d) with size (%d, %d), size of original matrix (%d, %d)", new Object[] { Integer.valueOf(startRow), Integer.valueOf(startColumn), Integer.valueOf(rowCount), Integer.valueOf(columnCount), Integer.valueOf(this.rowCount), Integer.valueOf(this.columnCount) }));
    }
    return new Matrix(rowCount, columnCount, this.numbers, this.startIndex + startColumn + this.underlyingColumnCount * startRow, this.underlyingColumnCount);
  }

  public Matrix add(Matrix m) { return copy().addEquals(m); }

  public Matrix subtract(Matrix m) { return copy().subtractEquals(m); }

  public Matrix multiply(double scalar) { return copy().multiplyEquals(scalar); }

  public Matrix multiply(Matrix m) {
    if (this.columnCount != m.rowCount) {
      throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
    }
    
    Matrix dest = new Matrix(this.rowCount, m.columnCount);
    
    for (int row = 0; row < dest.rowCount; row++) {
      for (int column = 0; column < dest.columnCount; column++) {
        dest.set(row, column, getRow(row).dot(m.getColumn(column)));
      }
    } 
    
    return dest;
  }

  public Vector multiply(Vector v) {
    if (this.columnCount != v.getDimension()) {
      throw new IllegalArgumentException("Vector length should equal the number of matrix columns");
    }
    
    Vector dest = new Vector(this.rowCount);
    
    for (int i = 0; i < this.rowCount; i++) {
      dest.set(i, v.dot(getRow(i)));
    }
    
    return dest;
  }

  public Matrix divide(double scalar) { return copy().divideEquals(scalar); }

  public Matrix inverse() { return LUDecomposition.decompose(this).inverse(); }

  public Matrix swapRows(int row1, int row2) { return copy().swapRowsEquals(row1, row2); }

  public Matrix transpose() {
    Matrix dest = new Matrix(this.columnCount, this.rowCount);
    
    for (int i = 0; i < this.columnCount; i++) {
      dest.setRow(i, getColumn(i));
    }
    
    return dest;
  }

  public Matrix addEquals(Matrix m) {
    if (this.rowCount != m.rowCount || this.columnCount != m.columnCount) {
      throw new IllegalArgumentException("Adding two matrices with different dimensions");
    }
    
    if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i] + m.numbers[m.startIndex + i];
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col) + m.get(row, col));
        }
      } 
    } 
    
    return this;
  }

  public Matrix subtractEquals(Matrix m) {
    if (this.rowCount != m.rowCount || this.columnCount != m.columnCount) {
      throw new IllegalArgumentException("Subtracting two matrices with different dimensions");
    }
    
    if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i] - m.numbers[m.startIndex + i];
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col) - m.get(row, col));
        }
      } 
    } 
    
    return this;
  }

  public Matrix multiplyEquals(double scalar) {
    if (this.columnCount == this.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i] * scalar;
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col) * scalar);
        }
      } 
    } 
    return this;
  }

  public Matrix multiplyEquals(Matrix m) {
    if (this.rowCount != this.columnCount || m.rowCount != m.columnCount || this.rowCount != m.columnCount) {
      throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
    }
    
    Matrix result = multiply(m);
    
    for (int i = 0; i < this.rowCount; i++) {
      setRow(i, result.getRow(i));
    }
    
    return this;
  }

  public Matrix divideEquals(double scalar) { return multiplyEquals(1.0D / scalar); }

  public Matrix swapRowsEquals(int row1, int row2) {
    Vector temp = getRow(row1).copy();
    setRow(row1, getRow(row2));
    setRow(row2, temp);
    return this;
  }

  public Matrix copy() {
    Matrix dest;
    if (this.columnCount == this.underlyingColumnCount) {
      dest = new Matrix(this.rowCount, this.columnCount);
      System.arraycopy(this.numbers, this.startIndex, dest.numbers, 0, dest.numbers.length);
    } else {
      dest = new Matrix(this.rowCount, this.columnCount, this::get);
    } 
    return dest;
  }

  public String toPrettyString() { return StringUtils.tableToString(this.rowCount, this.columnCount, (row, column) -> String.valueOf(get(row, column))); }

  public boolean equals(Matrix other, double tolerance) {
    if (this.rowCount != other.rowCount || this.columnCount != other.columnCount) {
      return false;
    }
    if (this.columnCount == this.underlyingColumnCount && other.columnCount == other.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        if (Math.abs(other.numbers[other.startIndex + i] - this.numbers[this.startIndex + i]) > tolerance) {
          return false;
        }
      } 
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          if (Math.abs(other.get(row, col) - get(row, col)) > tolerance) {
            return false;
          }
        } 
      } 
    } 
    return true;
  }

  public int hashCode() {
    int h = 0;
    for (int row = 0; row < this.rowCount; row++) {
      for (int col = 0; col < this.columnCount; col++) {
        h = 31 * h + Double.hashCode(get(row, col));
      }
    } 
    h = 31 * h + this.columnCount;
    return 31 * h + this.rowCount;
  }

  public boolean equals(Object other) {
    if (other == this) return true; 
    if (other == null || other.getClass() != Matrix.class) return false; 
    Matrix that = (Matrix)other;
    return equals(that, 0.0D);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    
    for (int i = 0; i < this.rowCount; i++) {
      sb.append(getRow(i)).append((i == this.rowCount - 1) ? "" : ", ");
    }
    
    return sb.append("}").toString();
  }

  public static Matrix fromString(String raw) {
    Matrix m = null;


    
    raw = raw.replaceAll("\\s+", "");
    
    if (!raw.startsWith("{") || !raw.endsWith("}")) {
      throw new IllegalArgumentException("Malformed matrix");
    }
    
    raw = raw.substring(2, raw.length() - 2);
    String[] data = raw.split("},\\{");
    int height = data.length;
    
    for (int i = 0; i < height; i++) {
      Vector v = Vector.fromString(data[i]);
      
      if (i == 0) {
        int width = v.getDimension();
        m = new Matrix(height, width);
      } 
      
      m.setRow(i, v);
    } 
    
    return m;
  }

  public static Matrix fromBigMatrix(BigMatrix m) {
    Matrix p = new Matrix(m.getRowCount(), m.getColumnCount());
    
    for (int i = 0; i < p.rowCount; i++) {
      p.setRow(i, Vector.fromBigVector(m.getRow(i)));
    }
    
    return p;
  }

  public static Matrix identityMatrix(int size) {
    Matrix m = new Matrix(size, size);
    
    for (int i = 0; i < size; i++) {
      m.set(i, i, 1.0D);
    }
    
    return m;
  }
  
  @FunctionalInterface
  public static interface DataProvider {
    double getValue(int param1Int1, int param1Int2);
  }
}
