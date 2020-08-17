package it.polimi.algorithm;

import it.polimi.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class BalVNS extends VNS {

    private float alpha;
    private float avg;

    public BalVNS(final int n, final int p, final float[][] d, final float alpha) {
        super(n, p, d);
        this.alpha = alpha;
        this.avg = (float) n / p;
    }

    @Override
    protected Pair<Integer, Float> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin) {
        // w is th change in the obj function obtained with the best interchange
        float w = 0;
        // v[j] is the change in the objective function obtained by deleting a facility currently in the solution,
        float[] v = new float[p];
        // for each location i, calculate swap of goin with c1[i]
        for (int i=0; i<n; i++) {
            int median = c1[i];
            int second = c2[i];
            int medianIdx = xidx[median];
            // if the location is closer to the new median instead of its c1[i]
            if (d[i][goin] < d[i][median]) {
                // goin become the new median of i replacing c1[i]. Update the change in obj
                w = w + d[i][goin] - d[i][median];
            } else {
                v[medianIdx] = v[medianIdx] + Math.min(d[i][goin], d[i][second]) - d[i][median];
            }
        }
        // oldc[j] = how many points where associated with median j before inserting goin
        int[] oldc = new int[p];
        for (int i=0; i<n; i++) {
            int first = c1[i], firstIdx = xidx[first];
            oldc[firstIdx] += 1;
        }
        // goinc[j] = how many points are associated with goin after removal of j
        int[] goinc = new int[p];
        // newc[i][j] = how many points are associated with median j after swap goin -> i
        int[][] newc = new int[p][p];
        // compute goinc and newc
        for (int i=0; i<n; i++) {
            int firstMedian = c1[i], firstMedianIdx = xidx[c1[i]];
            int secondMedian = c2[i], secondMedianIdx = xidx[c2[i]];
            // if, after the move, the point will be associated with goin
            if (d[i][goin] < d[i][firstMedian]) {
                // increment goin counter
                for (int j=0; j<p; j++)
                    goinc[j] += 1;
            } else {
                // the point remain with its original median
                for (int j=0; j<p; j++)
                    newc[j][firstMedianIdx] += 1;
                // except when the original median is removed
                newc[firstMedianIdx][firstMedianIdx] -= 1;
                // if goin is closer than secondMedian
                if (d[i][goin] < d[i][secondMedianIdx]) {
                    // goin gets a new point when firstMedian is removed
                    goinc[firstMedianIdx] += 1;
                } else {
                    // otherwise secondMedian gets a new point
                    newc[firstMedianIdx][secondMedianIdx] += 1;
                }
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

    @Override
    protected float computeObjectiveFunction(final int[] c1) {
        float w = 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<n; i++) {
            w += d[i][c1[i]];
            int count = counts.getOrDefault(c1[i], 0);
            counts.put(c1[i], count + 1);
        }

        for (Integer c : counts.values()) {
            float diff = c - avg;
            w += diff*diff;
        }

        return w;
    }
}
