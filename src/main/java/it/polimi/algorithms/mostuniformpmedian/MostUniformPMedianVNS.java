package it.polimi.algorithms.mostuniformpmedian;

import it.polimi.algorithms.pmedian.PMedianVNS;
import it.polimi.utils.Pair;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MostUniformPMedianVNS {
    private final Logger LOGGER = LoggerFactory.getLogger(MostUniformPMedianVNS.class);
    private final Random random = new Random();
    protected final int MAX_SOLUTION_CHANGES = 20;

    protected int n;
    protected int p;
    protected int kmax;
    protected float[][] d;

    private int[] medians;
    private int[] labels;
    private int objective;

    public MostUniformPMedianVNS(final int n, final int p, final float[][] d) {
        this(n, p, d, p);
    }

    public MostUniformPMedianVNS(final int n, final int p, final float[][] d, final int kmax) {
        this.n = n;
        this.p = p;
        this.kmax = kmax;
        this.d = d;
    }

    public int[] getMedians() {
        return medians;
    }

    public int[] getLabels() {
        return labels;
    }

    public int getObjective() {
        return objective;
    }

    public void run() {
        // optimal values
        int[] xopt = Rand.permutateRange(0, n);
        int[] xidxopt = getIndexes(xopt);
        int[][] c = getClosestMedians(xopt);
        int[] c1 = c[0];
        int[] c2 = c[1];
        int fopt = computeObjectiveFunction(c1);

        // current values
        int[] xcur = xopt.clone();
        int[] xidx = xidxopt.clone();
        int[] c1cur = c1.clone();
        int[] c2cur = c2.clone();
        int fcur = fopt;

        MostUniformFastInterchange mufi = new MostUniformFastInterchange(n, p, d);

        int k = 1;
        int changes = 0;
        while (k <= kmax) {
            // shaking
            for (int j = 1; j <= k; j++) {
                // sample random median to be inserted
                int goin = xcur[random.nextInt(n - p) + p];

                // find best median to remove
                Pair<Integer, Integer> pair = mufi.move(xcur, xidx, c1cur, c2cur, goin);
                int goout = pair.getFirst();
                fcur = pair.getSecond();

                // update xcur and xidx
                int outidx = xidx[goout], inidx = xidx[goin];
                xcur[outidx] = goin;
                xcur[inidx] = goout;
                xidx[goin] = outidx;
                xidx[goout] = inidx;

                // update c1 and c2
                mufi.update(xcur, c1cur, c2cur, goin, goout);
            }

            // Local search
            fcur = mufi.fastInterchange(xcur, xidx, c1cur, c2cur, fcur);

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

    private int computeObjectiveFunction(int[] c1) {
        float w = 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<n; i++) {
            int c = counts.getOrDefault(c1[i], 0);
            counts.put(c1[i], c + 1);
        }

        if (counts.values().size() == 0)
            return 0;

        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        for (int c : counts.values()) {
            if (c > max) max = c;
            if (c < min) min = c;
        }

        return max - min;
    }

}
