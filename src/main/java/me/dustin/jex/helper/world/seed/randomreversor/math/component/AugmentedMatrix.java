package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import me.dustin.jex.helper.world.seed.randomreversor.util.StringUtils;

public class AugmentedMatrix {
  private Matrix base;
  private Matrix extra;
  
  public AugmentedMatrix(Matrix base, Matrix extra) {
    this.base = base;
    this.extra = extra;
  }

  public Matrix getBase() { return this.base; }

  public Matrix getExtra() { return this.extra; }

  public void divideRow(int y, double scalar) {
    this.base.getRow(y).divideEquals(scalar);
    this.extra.getRow(y).divideEquals(scalar);
  }
  
  public void subtractScaledRow(int y1, double scalar, int y2) {
    this.base.getRow(y1).subtractEquals(this.base.getRow(y2).multiply(scalar));
    this.extra.getRow(y1).subtractEquals(this.extra.getRow(y2).multiply(scalar));
  }

  public String toString() {
    return StringUtils.tableToString(Math.max(this.base.getRowCount(), this.extra.getRowCount()), this.base.getColumnCount() + this.extra.getColumnCount(), (row, column) -> {
          if (column < this.base.getColumnCount()) {
            if (row >= this.base.getRowCount()) {
              return "";
            }
            return String.valueOf(this.base.get(row, column));
          } 
          
          column -= this.base.getColumnCount();
          if (row >= this.extra.getRowCount()) {
            return "";
          }
          return String.valueOf(this.extra.get(row, column));
        }, (row, column) -> {

          
          if (column == 0)
            return "["; 
          if (column == this.base.getColumnCount())
            return "|"; 
          if (column == this.base.getColumnCount() + this.extra.getColumnCount()) {
            return "]";
          }
          return " ";
        });
  }
}
