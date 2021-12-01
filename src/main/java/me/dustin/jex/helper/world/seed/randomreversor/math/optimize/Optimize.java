package me.dustin.jex.helper.world.seed.randomreversor.math.optimize;

import me.dustin.jex.helper.world.seed.randomreversor.util.*;
import java.util.*;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.*;

public class Optimize
{
    private final BigMatrix transform;
    private final BigMatrix table;
    private final int[] basics;
    private final int[] nonbasics;
    private final int rows;
    private final int cols;
    
    private Optimize(final BigMatrix table, final int[] basics, final int[] nonbasics, final BigMatrix transform) {
        this.table = table;
        this.basics = basics;
        this.nonbasics = nonbasics;
        this.transform = transform;
        this.rows = this.table.getRowCount();
        this.cols = this.table.getColumnCount();
    }
    
    private BigVector transformForTable(final BigVector lhs, final BigFraction rhs) {
        final BigVector transformed = new BigVector(this.transform.getColumnCount());
        final BigVector eliminated = new BigVector(this.cols);
        transformed.set(this.transform.getColumnCount() - 1, rhs);
        for (int row = 0; row < this.transform.getRowCount(); ++row) {
            final BigFraction x = lhs.get(row);
            transformed.subtractEquals(this.transform.getRow(row).multiply(x));
        }
        for (int col = 0; col < this.cols - 1; ++col) {
            eliminated.set(col, transformed.get(this.nonbasics[col]));
        }
        eliminated.set(this.cols - 1, transformed.get(this.transform.getColumnCount() - 1));
        for (int row = 0; row < this.rows - 1; ++row) {
            final BigFraction x = transformed.get(this.basics[row]);
            eliminated.subtractEquals(this.table.getRow(row).multiply(x));
        }
        return eliminated;
    }
    
    public Pair<BigVector, BigFraction> maximize(final BigVector gradient) {
        final Pair<BigVector, BigFraction> result = this.minimize(gradient.multiply(BigFraction.MINUS_ONE));
        return new Pair<BigVector, BigFraction>(result.getFirst(), result.getSecond().negate());
    }
    
    public Pair<BigVector, BigFraction> minimize(final BigVector gradient) {
        if (gradient.getDimension() != this.transform.getRowCount()) {
            throw new IllegalArgumentException("invalid size of gradient");
        }
        this.table.setRow(this.rows - 1, new BigVector(this.cols));
        this.table.getRow(this.rows - 1).subtractEquals(this.transformForTable(gradient, BigFraction.ZERO));
        this.solve();
        final BigVector result = this.transform.getColumn(this.transform.getColumnCount() - 1).copy();
        for (int row = 0; row < this.rows - 1; ++row) {
            final int v0 = this.basics[row];
            result.subtractEquals(this.transform.getColumn(v0).multiply(this.table.get(row, this.cols - 1)));
        }
        return new Pair<BigVector, BigFraction>(result, this.table.get(this.rows - 1, this.cols - 1));
    }
    
    private void solve() {
        while (this.step()) {}
    }
    
    private boolean step() {
        boolean bland = false;
        int entering = -1;
        int exiting = -1;
        BigFraction candidate = BigFraction.ZERO;
        for (int row = 0; row < this.rows - 1; ++row) {
            if (this.table.get(row, this.cols - 1).signum() == 0) {
                bland = true;
                break;
            }
        }
        for (int col = 0; col < this.cols - 1; ++col) {
            final BigFraction x = this.table.get(this.rows - 1, col);
            if (x.signum() > 0) {
                if (entering == -1 || x.compareTo(candidate) > 0) {
                    entering = col;
                    candidate = x;
                    if (bland) {
                        break;
                    }
                }
            }
        }
        if (entering == -1) {
            return false;
        }
        for (int row = 0; row < this.rows - 1; ++row) {
            final BigFraction x = this.table.get(row, entering);
            if (x.signum() > 0) {
                final BigFraction y = this.table.get(row, this.cols - 1).divide(x);
                if (exiting == -1 || y.compareTo(candidate) < 0) {
                    exiting = row;
                    candidate = y;
                }
            }
        }
        this.pivot(entering, exiting);
        return true;
    }
    
    private void pivot(final int entering, final int exiting) {
        final int rows = this.table.getRowCount();
        final int cols = this.table.getColumnCount();
        final int constraints = rows - 1;
        final int variables = cols - 1;
        assert 0 <= entering && entering < variables;
        assert 0 <= exiting && exiting < constraints;
        final BigFraction pivot = this.table.get(exiting, entering);
        for (int col = 0; col < cols; ++col) {
            if (col != entering) {
                this.table.set(exiting, col, this.table.get(exiting, col).divide(pivot));
            }
        }
        for (int row = 0; row < rows; ++row) {
            if (row != exiting) {
                final BigFraction x = this.table.get(row, entering);
                for (int col2 = 0; col2 < cols; ++col2) {
                    if (col2 != entering) {
                        final BigFraction y = this.table.get(exiting, col2);
                        this.table.set(row, col2, this.table.get(row, col2).subtract(x.multiply(y)));
                    }
                }
                this.table.set(row, entering, x.divide(pivot).negate());
            }
        }
        this.table.set(exiting, entering, pivot.reciprocal());
        final int temp = this.nonbasics[entering];
        this.nonbasics[entering] = this.basics[exiting];
        this.basics[exiting] = temp;
    }
    
    public Optimize copy() {
        return new Optimize(this.table.copy(), Arrays.copyOf(this.basics, this.rows - 1), Arrays.copyOf(this.nonbasics, this.cols - 1), this.transform);
    }
    
    public Optimize withStrictBound(final BigVector lhs, final BigFraction rhs) {
        final BigMatrix newTable = new BigMatrix(this.rows + 1, this.cols);
        for (int row = 0; row < this.rows - 1; ++row) {
            newTable.setRow(row, this.table.getRow(row));
        }
        newTable.setRow(this.rows - 1, this.transformForTable(lhs, rhs));
        if (newTable.get(this.rows - 1, this.cols - 1).signum() < 0) {
            newTable.getRow(this.rows - 1).multiplyEquals(BigFraction.MINUS_ONE);
        }
        final int[] newBasics = Arrays.copyOf(this.basics, this.rows);
        final int[] newNonbasics = Arrays.copyOf(this.nonbasics, this.cols - 1);
        newBasics[this.rows - 1] = this.rows - 1 + (this.cols - 1);
        return from(newTable, newBasics, newNonbasics, 1, this.transform);
    }
    
    private static Optimize from(final BigMatrix table, final int[] basics, final int[] nonbasics, final int artificials, final BigMatrix transform) {
        final int rows = table.getRowCount();
        final int cols = table.getColumnCount();
        final int realVariables = rows - 1 + (cols - 1) - artificials;
        for (int basicRow = 0; basicRow < rows - 1; ++basicRow) {
            if (basics[basicRow] >= realVariables) {
                table.getRow(rows - 1).addEquals(table.getRow(basicRow));
            }
        }
        final Optimize optimize = new Optimize(table, basics, nonbasics, null);
        optimize.solve();
        if (table.get(rows - 1, cols - 1).signum() != 0) {
            throw new IllegalArgumentException("table has no basic feasible solutions: " + table.get(rows - 1, cols - 1));
        }
        for (int row = 0; row < rows - 1; ++row) {
            if (basics[row] >= realVariables) {
                for (int col = 0; col < cols - 1; ++col) {
                    if (nonbasics[col] < realVariables && table.get(row, col).signum() != 0) {
                        optimize.pivot(col, row);
                        break;
                    }
                }
            }
        }
        final int finalCols = cols - artificials;
        final BigMatrix finalTable = new BigMatrix(rows, finalCols);
        for (int c0 = 0, c2 = 0; c0 < finalCols - 1; ++c0, ++c2) {
            while (nonbasics[c2] >= realVariables) {
                ++c2;
            }
            for (int row2 = 0; row2 < rows - 1; ++row2) {
                finalTable.set(row2, c0, table.get(row2, c2));
            }
            nonbasics[c0] = nonbasics[c2];
        }
        for (int row3 = 0; row3 < rows - 1; ++row3) {
            finalTable.set(row3, finalCols - 1, table.get(row3, cols - 1));
        }
        return new Optimize(finalTable, basics, nonbasics, transform);
    }
    
    private static Optimize from(final BigMatrix innerTable, final BigMatrix transform) {
        final int constraints = innerTable.getRowCount();
        final int variables = innerTable.getColumnCount() - 1;
        final int[] basics = new int[constraints];
        Arrays.fill(basics, -1);
        final List<Integer> nonbasicList = new ArrayList<Integer>();
        for (int row = 0; row < constraints; ++row) {
            if (innerTable.get(row, variables).signum() < 0) {
                innerTable.getRow(row).multiplyEquals(BigFraction.MINUS_ONE);
            }
        }
        int col = 0;
        final int j = 0;
        while (col < variables) {
            int count = 0;
            int index = -1;
            for (int row2 = 0; row2 < innerTable.getRowCount(); ++row2) {
                if (innerTable.get(row2, col).signum() != 0) {
                    ++count;
                    index = row2;
                }
            }
            if (count == 1 && basics[index] == -1 && innerTable.get(index, col).signum() > 0) {
                innerTable.getRow(index).divideEquals(innerTable.get(index, col));
                basics[index] = col;
            }
            else {
                nonbasicList.add(col);
            }
            ++col;
        }
        int artificials = 0;
        for (int row3 = 0; row3 < constraints; ++row3) {
            if (basics[row3] == -1) {
                basics[row3] = variables + artificials;
                ++artificials;
            }
        }
        final int[] nonbasics = nonbasicList.stream().mapToInt(i -> i).toArray();
        final int nonbasicCount = variables - constraints + artificials;
        final BigMatrix table = new BigMatrix(constraints + 1, nonbasicCount + 1);
        for (int row2 = 0; row2 < constraints; ++row2) {
            for (int basicRow = 0; basicRow < constraints; ++basicRow) {
                if (row2 != basicRow) {
                    if (basics[basicRow] < variables) {
                        final BigVector rowVector = innerTable.getRow(row2);
                        final BigVector basicVector = innerTable.getRow(basicRow);
                        rowVector.subtractEquals(basicVector.multiply(rowVector.get(basics[basicRow])));
                    }
                }
            }
            for (int col2 = 0; col2 < nonbasicCount; ++col2) {
                table.set(row2, col2, innerTable.get(row2, nonbasics[col2]));
            }
            table.set(row2, nonbasicCount, innerTable.get(row2, variables));
        }
        return from(table, basics, nonbasics, artificials, transform);
    }
    
    public static class Builder
    {
        private final int size;
        private final List<Integer> slacks;
        private final List<BigVector> lefts;
        private final List<BigFraction> rights;
        
        private Builder(final int size) {
            this.size = size;
            this.slacks = new ArrayList<Integer>();
            this.lefts = new ArrayList<BigVector>();
            this.rights = new ArrayList<BigFraction>();
        }
        
        public static Builder ofSize(final int size) {
            return new Builder(size);
        }
        
        private void checkLHS(final BigVector lhs) {
            if (lhs.getDimension() != this.size) {
                throw new IllegalArgumentException("invalid size of lhs: " + lhs.getDimension());
            }
        }
        
        public Optimize build() {
            final int variables = this.size + this.slacks.size();
            int constraint = 0;
            int slack = this.size;
            final BigMatrix table = new BigMatrix(this.slacks.size() + this.size, variables + 2 * this.size + 1);
            while (constraint < this.slacks.size()) {
                for (int col2 = 0; col2 < this.size; ++col2) {
                    table.set(constraint, col2, this.lefts.get(constraint).get(col2));
                }
                table.set(constraint, variables + 2 * this.size, this.rights.get(constraint));
                if (this.slacks.get(constraint) != 0) {
                    table.set(constraint, slack, new BigFraction(this.slacks.get(constraint)));
                    ++slack;
                }
                ++constraint;
            }
            int[] pivotRows = GaussJordan.reduce(table, (col, rows) -> col < this.size);
            for (int col3 = 0; col3 < this.size; ++col3) {
                if (pivotRows[col3] == -1) {
                    table.getRow(constraint).set(col3, BigFraction.ONE);
                    table.getRow(constraint).set(slack, BigFraction.ONE);
                    table.getRow(constraint).set(slack + 1, BigFraction.MINUS_ONE);
                    ++constraint;
                    slack += 2;
                }
            }
            pivotRows = GaussJordan.reduce(table);
            for (int col3 = 0; col3 < this.size; ++col3) {
                if (pivotRows[col3] == -1) {
                    throw new IllegalStateException("something went wrong? couldn't remove column from table");
                }
            }
            constraint = 1 + Arrays.stream(pivotRows).max().orElse(-1);
            final BigMatrix transform = new BigMatrix(this.size, slack - this.size + 1);
            final BigMatrix innerTable = new BigMatrix(constraint - this.size, slack - this.size + 1);
            for (int row = 0; row < this.size; ++row) {
                for (int col4 = 0; col4 < slack - this.size; ++col4) {
                    transform.set(row, col4, table.get(row, this.size + col4));
                }
                transform.set(row, slack - this.size, table.get(row, variables + 2 * this.size));
            }
            for (int row = 0; row < constraint - this.size; ++row) {
                for (int col4 = 0; col4 < slack - this.size; ++col4) {
                    innerTable.set(row, col4, table.get(this.size + row, this.size + col4));
                }
                innerTable.set(row, slack - this.size, table.get(this.size + row, variables + 2 * this.size));
            }
            return from(innerTable, transform);
        }
        
        private void checkLHS(final int lhs) {
            if (0 > lhs || lhs >= this.size) {
                throw new IllegalArgumentException("invalid index of lhs: " + lhs);
            }
        }
        
        private void add(final int slack, final BigVector lhs, final BigFraction rhs) {
            this.slacks.add(slack);
            this.lefts.add(lhs);
            this.rights.add(rhs);
        }
        
        public Builder withLowerBound(final BigVector lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(-1, lhs.copy(), rhs);
            return this;
        }
        
        public Builder withUpperBound(final BigVector lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(1, lhs.copy(), rhs);
            return this;
        }
        
        public Builder withStrictBound(final BigVector lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(0, lhs.copy(), rhs);
            return this;
        }
        
        public Builder withLowerBound(final int lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(-1, BigVector.basis(this.size, lhs), rhs);
            return this;
        }
        
        public Builder withUpperBound(final int lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(1, BigVector.basis(this.size, lhs), rhs);
            return this;
        }
        
        public Builder withStrictBound(final int lhs, final BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(0, BigVector.basis(this.size, lhs), rhs);
            return this;
        }
    }
}
