package it.polimi.algorithms.mpbpmp.domain;

import it.polimi.distances.Distance;
import it.polimi.domain.Problem;

import java.util.List;

public class MPBPMProblem implements Problem {
    private final int n;
    private final int p;
    private final int m;
    private final float[][] c;
    private final int[] r;
    private final int[] d;
    private final double avg;
    private final double alpha;
    private final int kmax;

    public MPBPMProblem(List<Service> services, int numPeriods, int numMedians, Distance distance) {
        this.n = services.size();
        this.p = numMedians;
        this.m = numPeriods;
        this.c = distance.getDistancesMatrix();
        this.r = services.stream().mapToInt(Service::getReleaseDate).toArray();
        this.d = services.stream().mapToInt(Service::getDueDate).toArray();
        this.avg = (double) this.n / (this.p * this.m);
        this.alpha = computeAlpha();
        this.kmax = getKMax();
    }

    public int getN() {
        return n;
    }

    public int getP() {
        return p;
    }

    public int getM() {
        return m;
    }

    public float[][] getC() {
        return c;
    }

    public int[] getR() {
        return r;
    }

    public int[] getD() {
        return d;
    }

    public double getAvg() {
        return avg;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getKmax() {
        return kmax;
    }

    private float computeAlpha() {
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += c[i][j];
                    count += 1;
                }
            }
        }
        return 0.2f * distSum / count;
    }

    private int getKMax() {
        int count = 0;
        for (int i=0; i<n; i++)
            count += (d[i] - r[i] == 0) ? 0 : 1;
        return Math.max(1, count);
    }
}
