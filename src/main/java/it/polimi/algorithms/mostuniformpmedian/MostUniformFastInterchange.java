package it.polimi.algorithms.mostuniformpmedian;

import it.polimi.utils.Pair;

public class MostUniformFastInterchange {

    private int n;
    private int p;
    private float[][] d;

    public MostUniformFastInterchange(final int n, final int p, final float[][] d) {
        this.n = n;
        this.p = p;
        this.d = d;
    }

    public int fastInterchange(int[] xopt, int[] xidx, int[] c1, int[] c2, int fopt) {
        while(true) {
            // find optimal goin and gout
            int wopt = Integer.MAX_VALUE;
            int goinopt = -1, gooutopt = -1;
            for (int i=p; i < n; i++) {
                int goin = xopt[i];
                Pair<Integer, Integer> pair = move(xopt, xidx, c1, c2, goin);
                int goout = pair.getFirst();
                int w = pair.getSecond();
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

    public Pair<Integer, Integer> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin) {
        // v[i][j] = how many customers are associated to median j after deleting median i
        int[][] v = new int[p][p];
        // goinc[i] = how many customers are associated to goin after deleting median i
        int[] goinc = new int[p];

        // get original counts for each median in v
        for (int i=0; i<n; i++)
            for (int j=0; j<p; j++)
                v[j][xidx[c1[i]]] += 1;

        // set deleted counts to null
        for (int j=0; j<p; j++)
            v[j][j] = 0;

        for (int i=0; i<n; i++) {
            if (d[i][goin] < d[i][c1[i]]) {
                for (int j=0; j<p; j++) {
                    goinc[j] += 1; // update goinc in each possible deletion
                    if (j != xidx[c1[i]])
                        v[j][xidx[c1[i]]] -= 1; // decrease count on all others, except for deleted one
                }
            } else {
                if (d[i][goin] < d[i][c2[i]])  {
                    goinc[xidx[c1[i]]] += 1; // if goinc closest then second median and deleting first median, count it
                } else {
                    v[xidx[c1[i]]][xidx[c2[i]]] += 1; // else count it in the second median
                }
            }
        }

        int zopt = Integer.MAX_VALUE;
        int goout = -1;
        for (int i=0; i<p; i++) {
            int max = goinc[i], min = goinc[i];
            for (int j=0; j<p; j++) {
                if (i == j) continue;
                if (v[i][j] > max) {
                    max = v[i][j];
                }
                if (v[i][j] < min) {
                    min = v[i][j];
                }
            }

            int z = max - min;

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
