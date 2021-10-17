package me.dustin.jex.helper.world.seed.randomreversor.math.component;

import me.dustin.jex.helper.world.seed.randomreversor.util.StringUtils;

public class BigAugmentedMatrix {
  private BigMatrix base;
  private BigMatrix extra;
  
  public BigAugmentedMatrix(BigMatrix base, BigMatrix extra) {
    this.base = base;
    this.extra = extra;
  }

  public BigMatrix getBase() { return this.base; }

  public BigMatrix getExtra() { return this.extra; }

  public void divideRow(int y, BigFraction scalar) {
    this.base.getRow(y).divideEquals(scalar);
    this.extra.getRow(y).divideEquals(scalar);
  }
  
  public void subtractScaledRow(int y1, BigFraction scalar, int y2) {
    this.base.getRow(y1).subtractEquals(this.base.getRow(y2).multiply(scalar));
    this.extra.getRow(y1).subtractEquals(this.extra.getRow(y2).multiply(scalar));
  }

  public String toString() {
    return StringUtils.tableToString(Math.max(this.base.getRowCount(), this.extra.getRowCount()), this.base.getColumnCount() + this.extra.getColumnCount(), (row, column) -> {
          if (column < this.base.getColumnCount()) {
            if (row >= this.base.getRowCount()) {
              return "";
            }
            return this.base.get(row, column).toString();
          } 
          
          column -= this.base.getColumnCount();
          if (row >= this.extra.getRowCount()) {
            return "";
          }
          return this.extra.get(row, column).toString();
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
