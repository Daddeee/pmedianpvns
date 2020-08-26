package it.polimi.algorithms.pmedian;

import it.polimi.utils.Pair;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PMedianVNS {
    private final Logger LOGGER = LoggerFactory.getLogger(PMedianVNS.class);
    private final Random random = new Random();
    protected final int MAX_SOLUTION_CHANGES = 20;

    protected int n;
    protected int p;
    protected int kmax;
    protected float[][] d;

    private int[] medians;
    private int[] labels;
    private float objective;

    public PMedianVNS(final int n, final int p, final float[][] d) {
        this.n = n;
        this.p = p;
        this.kmax = p;
        this.d = d;
    }

    public int[] getLabels() {
        return labels;
    }

    public int[] getMedians() {
        return medians;
    }

    public float getObjective() {
        return objective;
    }

    public void run() {
        // optimal values
        int[] xopt = Rand.permutateRange(0, n);
        int[] xidxopt = getIndexes(xopt);
        int[][] c = getClosestMedians(xopt);
        int[] c1 = c[0];
        int[] c2 = c[1];
        float fopt = computeObjectiveFunction(c1);

        // current values
        int[] xcur = xopt.clone();
        int[] xidx = xidxopt.clone();
        int[] c1cur = c1.clone();
        int[] c2cur = c2.clone();
        float fcur = fopt;

        int k = 1;
        int changes = 0;
        while (k < kmax) {
            // shaking
            for (int j = 1; j <= k; j++) {
                // sample random median to be inserted
                int goin = random.nextInt(n - p) + p;

                // find best median to remove
                Pair<Integer, Float> pair = move(xcur, xidx, c1cur, c2cur, goin);
                int goout = pair.getFirst();
                float w = pair.getSecond();

                // update obj function
                fcur = fcur + w;

                // update xcur and xidx
                int outidx = xidx[goout], inidx = xidx[goin];
                xcur[outidx] = goin;
                xcur[inidx] = goout;
                xidx[goin] = outidx;
                xidx[goout] = inidx;

                // update c1 and c2
                update(xcur, c1cur, c2cur, goin, goout);
            }

            // Local search
            fcur = fastInterchange(xcur, xidx, c1cur, c2cur, fcur);

            // Move or not
            if (fcur < fopt) {
                fopt = fcur;
                xopt = xcur.clone();
                xidxopt = xidx.clone();
                c1 = c1cur.clone();
                c2 = c2cur.clone();
                k = 1;

                changes++;
                if (changes >= MAX_SOLUTION_CHANGES) {
                    LOGGER.info("Max solution changes limit hit.");
                    break;
                }
            } else {
                fcur = fopt;
                xcur = xopt.clone();
                xidx = xidxopt.clone();
                c1cur = c1.clone();
                c2cur = c2.clone();
                k = k + 1;
            }
        }

        this.medians = Arrays.copyOfRange(xopt, 0, p);
        this.labels = c1;
        this.objective = fopt;
    }

    // arr contains numbers 0 to n
    private int[] getIndexes(int[] arr) {
        int[] idxs = new int[arr.length];
        for (int i=0; i<arr.length; i++) {
            idxs[arr[i]] = i;
        }
        return idxs;
    }

    private int[][] getClosestMedians(int[] x) {
        int[][] c = new int[2][n];

        // for each location
        for (int i=0; i<n; i++) {
            // initialize the 2 closest distances from medians
            float firstMin = Float.MAX_VALUE, secondMin = Float.MAX_VALUE;
            // for each median in x
            for (int j=0; j<p; j++) {
                // get distance from location
                float dist = d[i][x[j]];

                // if it's less than firstMin update both values and indexes
                if (dist < firstMin) {
                    secondMin = firstMin;
                    firstMin = dist;
                    c[1][i] = c[0][i];
                    c[0][i] = x[j];
                } else if (dist < secondMin) {
                    // otherwise if it's less than secondMin update only second indexes
                    secondMin = dist;
                    c[1][i] = x[j];
                }
            }
        }

        return c;
    }

    protected float computeObjectiveFunction(int[] c1) {
        float w = 0;
        for (int i=0; i<n; i++) {
            w += d[i][c1[i]];
        }
        return w;
    }

    protected Pair<Integer, Float> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin) {
        // w is th change in the obj function obtained with the best interchange
        float w = 0;
        // v[j] is the change in the objective function obtained by deleting a facility currently in the solution,
        float[] v = new float[p];

        // TODO UPDATE HERE TO INCLUDE BALANCE
        // for each location i, calculate swap of goin with c1[i]
        for (int i=0; i<n; i++) {
            // if the location is closer to the new median instead of its c1[i]
            if (d[i][goin] < d[i][c1[i]]) {
                // goin become the new median of i replacing of c1[i]. Update the change in obj
                w = w + d[i][goin] - d[i][c1[i]];
            } else {
                // calculate the cost of deleting c1[i] from solution
                v[xidx[c1[i]]] = v[xidx[c1[i]]] + Math.min(d[i][goin], d[i][c2[i]]) - d[i][c1[i]];
            }
        }

        float g = Float.MAX_VALUE;
        int goout = -1;
        for (int i=0; i<p; i++) {
            if (v[i] < g) {
                g = v[i];
                goout = x[i];
            }
        }

        w = w + g;

        return new Pair<>(goout, w);
    }

    private void update(int[]x,  int[] c1, int[] c2, int goin, int goout) {
        // updates c1 and c2 for each location by replacing goout with goin
        for (int i=0; i<n; i++) {
            // if goout is current median
            if (c1[i] == goout) {
                // if goin is closer to i than the second median c2[i]
                if (d[i][goin] <= d[i][c2[i]]) {
                    // it becomes the new median
                    c1[i] = goin;
                } else {
                    // otherwise c2[i] becomes the new median
                    c1[i] = c2[i];

                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i, x, c1);
                }
            } else {
                if (d[i][goin] < d[i][c1[i]]) {
                    c2[i] = c1[i];
                    c1[i] = goin;
                } else if (d[i][goin] < d[i][c2[i]]) {
                    c2[i] = goin;
                } else if (c2[i] == goout) {
                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i,x, c1);
                }
            }
        }
    }

    private int searchSecondMedian(int i, int[] x, int[] c1) {
        //  TODO: maybe use max heap
        float newMin = Float.MAX_VALUE;
        int secondMedian = -1;
        for (int j=0; j<p; j++) {
            if (x[j] != c1[i] && d[i][x[j]] < newMin) {
                newMin = d[i][x[j]];
                secondMedian = x[j];
            }
        }
        return secondMedian;
    }

    private float fastInterchange(int[] xopt, int[] xidx, int[] c1, int[] c2, float fopt) {
        while(true) {
            // find optimal goin and gout
            float wopt = Float.MAX_VALUE;
            int goinopt = -1, gooutopt = -1;
            for (int i=p; i < n; i++) {
                int goin = xopt[i];
                Pair<Integer, Float> pair = move(xopt, xidx, c1, c2, goin);
                int goout = pair.getFirst();
                float w = pair.getSecond();
                if (w < wopt) {
                    wopt = w;
                    goinopt = goin;
                    gooutopt = goout;
                }
            }

            // no improvement found
            if (wopt >= 0)
                return fopt;

            // update obj function
            fopt = fopt + wopt;

            // swap optimal goin and goout
            int outidx = xidx[gooutopt], inidx = xidx[goinopt];
            xopt[outidx] = goinopt;
            xopt[inidx] = gooutopt;
            xidx[goinopt] = outidx;
            xidx[gooutopt] = inidx;

            update(xopt, c1, c2, goinopt, gooutopt);
        }
    }
}
