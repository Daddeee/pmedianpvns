package it.polimi.algorithms.balancedpmedian;

import it.polimi.utils.Pair;

public class BalancedFastInterchange {

    private int n;
    private int p;
    private float[][] d;
    private float alpha;
    private float avg;

    public BalancedFastInterchange(final int n, final int p, final float[][] d, final float alpha, final float avg) {
        this.n = n;
        this.p = p;
        this.d = d;
        this.alpha = alpha;
        this.avg = avg;
    }

    public float fastInterchange(int[] xopt, int[] xidx, int[] c1, int[] c2, float fopt) {
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
            fopt = wopt;

            // swap optimal goin and goout
            int outidx = xidx[gooutopt], inidx = xidx[goinopt];
            xopt[outidx] = goinopt;
            xopt[inidx] = gooutopt;
            xidx[goinopt] = outidx;
            xidx[gooutopt] = inidx;

            update(xopt, c1, c2, goinopt, gooutopt);
        }
    }

    public Pair<Integer, Float> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin) {
        // counts[i][j] = how many customers are associated to median j after deleting median i
        int[][] counts = new int[p][p];
        // goincount[i] = how many customers are associated to goin after deleting median i
        int[] goincount = new int[p];
        // dists[j] is the change in the objective function obtained by deleting a facility currently in the solution,
        float[][] dists = new float[p][p];
        // w is th change in the obj function obtained with the best interchange
        float[] goindist = new float[p];

        // get original counts and distrances for each median
        for (int i=0; i<n; i++) {
            for (int j=0; j<p; j++) {
                counts[j][xidx[c1[i]]] += 1;
                dists[j][xidx[c1[i]]] += d[i][c1[i]];
            }
        }

        // set deleted counts and distances to null
        for (int j=0; j<p; j++) {
            counts[j][j] = 0;
            dists[j][j] = 0;
        }

        for (int i=0; i<n; i++) {
            if (d[i][goin] < d[i][c1[i]]) {
                for (int j=0; j<p; j++) {
                    goincount[j] += 1; // update goincount in each possible deletion
                    goindist[j] += d[i][goin];
                    if (j != xidx[c1[i]]) {
                        counts[j][xidx[c1[i]]] -= 1; // decrease count on all others, except for deleted one
                        dists[j][xidx[c1[i]]] -= d[i][c1[i]];
                    }
                }
            } else {
                if (d[i][goin] < d[i][c2[i]])  {
                    goincount[xidx[c1[i]]] += 1; // if goincount closest then second median and deleting first median, count it
                    goindist[xidx[c1[i]]] += d[i][goin];
                } else {
                    counts[xidx[c1[i]]][xidx[c2[i]]] += 1; // else count it in the second median
                    dists[xidx[c1[i]]][xidx[c2[i]]] += d[i][c2[i]];
                }
            }
        }

        float zopt = Float.MAX_VALUE;
        int goout = -1;
        for (int i=0; i<p; i++) {

            float z = goindist[i] + alpha * Math.abs(goincount[i] - avg);

            for (int j=0; j<p; j++) {
                if (j == i) continue;
                z += dists[i][j];
                z += alpha * Math.abs(counts[i][j] - avg);
            }

            if (z < zopt) {
                zopt = z;
                goout = i;
            }
        }

        return new Pair<>(x[goout], zopt);
    }

    public void update(int[]x,  int[] c1, int[] c2, int goin, int goout) {
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
}
