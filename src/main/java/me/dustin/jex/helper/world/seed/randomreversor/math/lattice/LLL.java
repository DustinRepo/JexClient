package me.dustin.jex.helper.world.seed.randomreversor.math.lattice;

import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigFraction;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigMatrix;
import me.dustin.jex.helper.world.seed.randomreversor.math.component.BigVector;

public class LLL
{
    private BigMatrix gramSchmidtBasis;
    private BigMatrix mu;
    private BigMatrix lattice;
    private BigMatrix H;
    private BigFraction[] sizes;
    private Params params;
    private int kmax;
    private int k;
    private boolean shouldUpdateGramSchmidt;
    private static final BigFraction eta;
    
    private LLL() {
    }
    
    public static Result reduce(final BigMatrix lattice, final Params params) {
        return new LLL().reduce0(lattice, params);
    }
    
    private Result reduce0(final BigMatrix lattice, final Params params) {
        this.params = params;
        final int n = lattice.getRowCount();
        final int m = lattice.getColumnCount();
        this.gramSchmidtBasis = new BigMatrix(n, m);
        this.mu = new BigMatrix(n, n);
        this.k = 1;
        this.kmax = 0;
        this.gramSchmidtBasis.setRow(0, lattice.getRow(0).copy());
        this.shouldUpdateGramSchmidt = true;
        this.H = BigMatrix.identityMatrix(n);
        this.lattice = lattice.copy();
        (this.sizes = new BigFraction[n])[0] = this.lattice.getRow(0).magnitudeSq();
        while (this.k < n) {
            if (this.k > this.kmax && this.shouldUpdateGramSchmidt) {
                this.kmax = this.k;
                this.incGramSchmidt();
            }
            this.testCondition();
        }
        int p = 0;
        for (int i = 0; i < n; ++i) {
            if (this.lattice.getRow(i).isZero()) {
                ++p;
            }
        }
        final BigMatrix nonZeroLattice = new BigMatrix(n - p, m);
        for (int j = p; j < n; ++j) {
            nonZeroLattice.setRow(j - p, this.lattice.getRow(j));
        }
        return new Result(p, nonZeroLattice, this.H);
    }
    
    private void incGramSchmidt() {
        for (int j = 0; j <= this.k - 1; ++j) {
            if (this.sizes[j].compareTo(BigFraction.ZERO) != 0) {
                this.mu.set(this.k, j, this.lattice.getRow(this.k).dot(this.gramSchmidtBasis.getRow(j)).divide(this.sizes[j]));
            }
            else {
                this.mu.set(this.k, j, BigFraction.ZERO);
            }
        }
        final BigVector newRow = this.lattice.getRow(this.k).copy();
        for (int i = 0; i <= this.k - 1; ++i) {
            newRow.subtractEquals(this.gramSchmidtBasis.getRow(i).multiply(this.mu.get(this.k, i)));
        }
        this.gramSchmidtBasis.setRow(this.k, newRow);
        this.sizes[this.k] = newRow.magnitudeSq();
    }
    
    private void testCondition() {
        this.red(this.k, this.k - 1);
        if (this.sizes[this.k].toDouble() < (this.params.delta - this.mu.get(this.k, this.k - 1).multiply(this.mu.get(this.k, this.k - 1)).toDouble()) * this.sizes[this.k - 1].toDouble()) {
            this.swapg(this.k);
            this.k = Math.max(1, this.k - 1);
            this.shouldUpdateGramSchmidt = false;
        }
        else {
            this.shouldUpdateGramSchmidt = true;
            for (int l = this.k - 2; l >= 0; --l) {
                this.red(this.k, l);
            }
            ++this.k;
        }
    }
    
    private void swapg(final int n) {
        this.lattice.swapRowsEquals(n, n - 1);
        this.H.swapRowsEquals(n, n - 1);
        if (n > 1) {
            for (int j = 0; j <= n - 2; ++j) {
                final BigFraction temp = this.mu.get(n, j);
                this.mu.set(n, j, this.mu.get(n - 1, j));
                this.mu.set(n - 1, j, temp);
            }
        }
        final BigFraction mutwopointoh = this.mu.get(n, n - 1);
        final BigFraction B = this.sizes[n].add(mutwopointoh.multiply(mutwopointoh).multiply(this.sizes[n - 1]));
        if (this.sizes[n].equals(BigFraction.ZERO) && mutwopointoh.equals(BigFraction.ZERO)) {
            BigFraction temp2 = this.sizes[n];
            this.sizes[n] = this.sizes[n - 1];
            this.sizes[n - 1] = temp2;
            this.gramSchmidtBasis.swapRowsEquals(n, n - 1);
            for (int i = n + 1; i <= this.kmax; ++i) {
                temp2 = this.mu.get(i, n);
                this.mu.set(i, n, this.mu.get(i, n - 1));
                this.mu.set(i, n - 1, temp2);
            }
        }
        else if (this.sizes[n].equals(BigFraction.ZERO)) {
            this.sizes[n - 1] = B;
            this.gramSchmidtBasis.getRow(n - 1).multiplyEquals(mutwopointoh);
            this.mu.set(n, n - 1, BigFraction.ONE.divide(mutwopointoh));
            for (int k = n + 1; k <= this.kmax; ++k) {
                this.mu.set(k, n - 1, this.mu.get(k, n - 1).divide(mutwopointoh));
            }
        }
        else {
            BigFraction t = this.sizes[n - 1].divide(B);
            this.mu.set(n, n - 1, mutwopointoh.multiply(t));
            final BigVector b = this.gramSchmidtBasis.getRow(n - 1).copy();
            this.gramSchmidtBasis.setRow(n - 1, this.gramSchmidtBasis.getRow(n).add(b.multiply(mutwopointoh)));
            this.gramSchmidtBasis.setRow(n, b.multiply(this.sizes[this.k].divide(B)).subtract(this.gramSchmidtBasis.getRow(n).multiply(this.mu.get(n, n - 1))));
            this.sizes[n] = this.sizes[n].multiply(t);
            this.sizes[n - 1] = B;
            for (int l = n + 1; l <= this.kmax; ++l) {
                t = this.mu.get(l, n);
                this.mu.set(l, n, this.mu.get(l, n - 1).subtract(mutwopointoh.multiply(t)));
                this.mu.set(l, n - 1, t.add(this.mu.get(n, n - 1).multiply(this.mu.get(l, n))));
            }
        }
    }
    
    private void red(final int n, final int l) {
        if (this.mu.get(n, l).abs().compareTo(LLL.eta) <= 0) {
            return;
        }
        final BigFraction q = new BigFraction(this.mu.get(n, l).round());
        this.lattice.setRow(n, this.lattice.getRow(n).subtract(this.lattice.getRow(l).multiply(q)));
        this.H.setRow(n, this.H.getRow(n).subtract(this.H.getRow(l).multiply(q)));
        this.mu.set(n, l, this.mu.get(n, l).subtract(q));
        for (int i = 0; i <= l - 1; ++i) {
            this.mu.set(n, i, this.mu.get(n, i).subtract(this.mu.get(l, i).multiply(q)));
        }
    }
    
    static {
        eta = BigFraction.HALF;
    }
    
    public static final class Params
    {
        protected double delta;
        protected boolean debug;
        
        public Params() {
            this.delta = 0.75;
        }
        
        public Params setDelta(final double delta) {
            this.delta = delta;
            return this;
        }
        
        public Params setDebug(final boolean debug) {
            this.debug = debug;
            return this;
        }
    }
    
    public static final class Result
    {
        private int numDependantVectors;
        private BigMatrix reducedBasis;
        private BigMatrix transformationsDone;
        
        private Result(final int numDependantVectors, final BigMatrix reducedBasis, final BigMatrix transformationsDone) {
            this.numDependantVectors = numDependantVectors;
            this.reducedBasis = reducedBasis;
            this.transformationsDone = transformationsDone;
        }
        
        public int getNumDependantVectors() {
            return this.numDependantVectors;
        }
        
        public BigMatrix getReducedBasis() {
            return this.reducedBasis;
        }
        
        public BigMatrix getTransformations() {
            return this.transformationsDone;
        }
    }
}
