package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import me.dustin.jex.helper.world.seed.randomreversor.math.decomposition.LUDecomposition;
import me.dustin.jex.helper.world.seed.randomreversor.util.StringUtils;

import java.util.Arrays;

public final class BigMatrix {
  private BigFraction[] numbers;
  private int rowCount;
  private int columnCount;
  private int startIndex;
  private int underlyingColumnCount;
  
  public BigMatrix(int rowCount, int columnCount) {
    this.startIndex = 0;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.underlyingColumnCount = columnCount;
    
    if (rowCount <= 0 || columnCount <= 0) {
      throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
    }
    
    this.numbers = new BigFraction[rowCount * columnCount];
    Arrays.fill(this.numbers, BigFraction.ZERO);
  }

  public BigMatrix(int rowCount, int columnCount, DataProvider gen) {
    this.startIndex = 0;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
    this.underlyingColumnCount = columnCount;
    
    if (rowCount <= 0 || columnCount <= 0) {
      throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
    }
    
    this.numbers = new BigFraction[rowCount * columnCount];
    for (int row = 0; row < rowCount; row++) {
      for (int column = 0; column < columnCount; column++)
        this.numbers[column + columnCount * row] = gen.getValue(row, column); 
    } 
  }
  
  private BigMatrix(int rowCount, int columnCount, BigFraction[] numbers, int startIndex, int underlyingColumnCount) {
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

  public BigFraction get(int row, int col) {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
    }
    return this.numbers[this.startIndex + col + this.underlyingColumnCount * row];
  }

  public void set(int row, int col, BigFraction value) {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
    }
    this.numbers[this.startIndex + col + this.underlyingColumnCount * row] = value;
  }


  public BigVector getRow(int rowIndex) {
    if (rowIndex < 0 || rowIndex >= this.rowCount) {
      throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
    }
    return BigVector.createView(this.numbers, this.columnCount, this.startIndex + rowIndex * this.underlyingColumnCount, 1);
  }

  public BigVector getColumn(int columnIndex) {
    if (columnIndex < 0 || columnIndex >= this.columnCount) {
      throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
    }
    return BigVector.createView(this.numbers, this.rowCount, this.startIndex + columnIndex, this.underlyingColumnCount);
  }

  public void setRow(int rowIndex, BigVector newRow) {
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

  public void setColumn(int columnIndex, BigVector newColumn) {
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

  public BigMatrix submatrix(int startRow, int startColumn, int rowCount, int columnCount) {
    if (startRow < 0 || startColumn < 0 || rowCount <= 0 || columnCount <= 0 || startRow + rowCount >= this.rowCount || startColumn + columnCount >= this.columnCount) {
      throw new IllegalArgumentException(String.format("Illegal submatrix start (%d, %d) with size (%d, %d), size of original matrix (%d, %d)", new Object[] { Integer.valueOf(startRow), Integer.valueOf(startColumn), Integer.valueOf(rowCount), Integer.valueOf(columnCount), Integer.valueOf(this.rowCount), Integer.valueOf(this.columnCount) }));
    }
    return new BigMatrix(rowCount, columnCount, this.numbers, this.startIndex + startColumn + this.underlyingColumnCount * startRow, this.underlyingColumnCount);
  }

  public BigMatrix add(BigMatrix m) { return copy().addEquals(m); }

  public BigMatrix subtract(BigMatrix m) { return copy().subtractEquals(m); }

  public BigMatrix multiply(BigFraction scalar) { return copy().multiplyEquals(scalar); }

  public BigMatrix multiply(BigMatrix m) {
    if (this.columnCount != m.rowCount) {
      throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
    }
    
    BigMatrix dest = new BigMatrix(this.rowCount, m.columnCount);
    
    for (int row = 0; row < dest.rowCount; row++) {
      for (int column = 0; column < dest.columnCount; column++) {
        dest.set(row, column, getRow(row).dot(m.getColumn(column)));
      }
    } 
    
    return dest;
  }

  public BigVector multiply(BigVector v) {
    if (this.columnCount != v.getDimension()) {
      throw new IllegalArgumentException("Vector length should equal the number of matrix columns");
    }
    
    BigVector dest = new BigVector(this.rowCount);
    
    for (int i = 0; i < this.rowCount; i++) {
      dest.set(i, v.dot(getRow(i)));
    }
    
    return dest;
  }

  public BigMatrix divide(BigFraction scalar) { return multiply(scalar.reciprocal()); }

  public BigMatrix inverse() { return LUDecomposition.decompose(this).inverse(); }

  public BigMatrix swapRows(int row1, int row2) { return copy().swapRowsEquals(row1, row2); }

  public BigMatrix transpose() {
    BigMatrix dest = new BigMatrix(getColumnCount(), getRowCount());
    
    for (int i = 0; i < this.columnCount; i++) {
      dest.setRow(i, getColumn(i));
    }
    
    return dest;
  }

  public BigMatrix addEquals(BigMatrix m) {
    if (getRowCount() != m.getRowCount() || getColumnCount() != m.getColumnCount()) {
      throw new IllegalArgumentException("Adding two matrices with different dimensions");
    }
    
    if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].add(m.numbers[m.startIndex + i]);
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col).add(m.get(row, col)));
        }
      } 
    } 
    
    return this;
  }

  public BigMatrix subtractEquals(BigMatrix m) {
    if (getRowCount() != m.getRowCount() || getColumnCount() != m.getColumnCount()) {
      throw new IllegalArgumentException("Subtracting two matrices with different dimensions");
    }
    
    if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].subtract(m.numbers[m.startIndex + i]);
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col).subtract(m.get(row, col)));
        }
      } 
    } 
    
    return this;
  }

  public BigMatrix multiplyEquals(BigFraction scalar) {
    if (this.columnCount == this.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].multiply(scalar);
      }
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          set(row, col, get(row, col).multiply(scalar));
        }
      } 
    } 
    return this;
  }

  public BigMatrix multiplyEquals(BigMatrix m) {
    if (this.rowCount != this.columnCount || m.rowCount != m.columnCount || this.rowCount != m.columnCount) {
      throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
    }
    
    BigMatrix result = multiply(m);
    
    for (int i = 0; i < getRowCount(); i++) {
      setRow(i, result.getRow(i));
    }
    
    return this;
  }

  public BigMatrix divideEquals(BigFraction scalar) { return multiplyEquals(scalar.reciprocal()); }

  public BigMatrix swapRowsEquals(int row1, int row2) {
    BigVector temp = getRow(row1).copy();
    setRow(row1, getRow(row2));
    setRow(row2, temp);
    return this;
  }

  public BigMatrix copy() {
    BigMatrix dest;
    if (this.columnCount == this.underlyingColumnCount) {
      dest = new BigMatrix(this.rowCount, this.columnCount);
      System.arraycopy(this.numbers, this.startIndex, dest.numbers, 0, dest.numbers.length);
    } else {
      dest = new BigMatrix(this.rowCount, this.columnCount, this::get);
    } 
    return dest;
  }

  public String toPrettyString() { return StringUtils.tableToString(getRowCount(), getColumnCount(), (row, column) -> get(row, column).toString()); }

  public int hashCode() {
    int h = 0;
    for (int row = 0; row < this.rowCount; row++) {
      for (int col = 0; col < this.columnCount; col++) {
        h = 31 * h + get(row, col).hashCode();
      }
    } 
    h = 31 * h + this.columnCount;
    return 31 * h + this.rowCount;
  }

  public boolean equals(Object other) {
    if (other == this) return true; 
    if (other == null || other.getClass() != BigMatrix.class) return false; 
    BigMatrix that = (BigMatrix)other;
    if (this.rowCount != that.rowCount || this.columnCount != that.columnCount) {
      return false;
    }
    if (this.columnCount == this.underlyingColumnCount && that.columnCount == that.underlyingColumnCount) {
      int size = this.rowCount * this.columnCount;
      for (int i = 0; i < size; i++) {
        if (!that.numbers[that.startIndex + i].equals(this.numbers[this.startIndex + i])) {
          return false;
        }
      } 
    } else {
      for (int row = 0; row < this.rowCount; row++) {
        for (int col = 0; col < this.columnCount; col++) {
          if (!that.get(row, col).equals(get(row, col))) {
            return false;
          }
        } 
      } 
    } 
    return true;
  }

  
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    
    for (int i = 0; i < getRowCount(); i++) {
      sb.append(getRow(i)).append((i == getRowCount() - 1) ? "" : ", ");
    }
    
    return sb.append("}").toString();
  }

  public static BigMatrix fromString(String raw) {
    BigMatrix m = null;
    raw = raw.replaceAll("\\s+", "");
    
    if (!raw.startsWith("{") || !raw.endsWith("}")) {
      throw new IllegalArgumentException("Malformed matrix");
    }
    
    raw = raw.substring(2, raw.length() - 2);
    String[] data = raw.split("},\\{");
    int height = data.length;
    
    for (int i = 0; i < height; i++) {
      BigVector v = BigVector.fromString(data[i]);
      
      if (i == 0) {
        int width = v.getDimension();
        m = new BigMatrix(height, width);
      } 
      
      m.setRow(i, v);
    } 
    
    return m;
  }

  public static BigMatrix identityMatrix(int size) {
    BigMatrix m = new BigMatrix(size, size);
    
    for (int i = 0; i < size; i++) {
      m.set(i, i, BigFraction.ONE);
    }
    
    return m;
  }
  
  @FunctionalInterface
  public static interface DataProvider {
    BigFraction getValue(int param1Int1, int param1Int2);
  }
}
