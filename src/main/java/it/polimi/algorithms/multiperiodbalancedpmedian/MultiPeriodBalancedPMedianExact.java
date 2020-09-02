package it.polimi.algorithms.multiperiodbalancedpmedian;

import com.ampl.*;
import com.ampl.Set;
import it.polimi.algorithms.ExactSolver;
import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Service;
import it.polimi.io.SpeedyReader;
import it.polimi.io.TestCSVReader;
import it.polimi.io.ZonesCSVWriter;
import it.polimi.utils.Pair;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MultiPeriodBalancedPMedianExact extends ExactSolver {

    // utils
    private String resultPath;
    private List<Service> services;

    // params
    private int n;
    private int p;
    private int m;
    private float[][] c;
    private int[] r;
    private int[] d;
    private float alpha;

    // solution
    private int[] medians;
    private int[] superMedians;
    private int[] periods;

    // initial solution
    private int[] initialPeriods;
    private int[] initialMedians;
    private int[] initialSuperMedians;

    public MultiPeriodBalancedPMedianExact(String modelPath, String resultPath, List<Service> services, Distance distance,
                                           int p, int m) {
        super(modelPath);
        this.n = services.size();
        this.p = p;
        this.m = m;
        this.resultPath = resultPath;
        parseParams(services, distance);
    }

    public int[] getMedians() {
        return medians;
    }

    public int[] getSuperMedians() {
        return superMedians;
    }

    public int[] getPeriods() {
        return periods;
    }

    private void parseParams(List<Service> services, Distance distance) {
        this.services = services;
        this.c = distance.getDurationsMatrix();
        this.r = new int[n];
        this.d = new int[n];
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            Service s = services.get(i);
            r[i] = s.getReleaseDate();
            d[i] = Math.min(r[i] + s.getDays(), m) - 1;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += c[i][j];
                    count += 1;
                }
            }
        }
        this.alpha = 0.2f * distSum / count;
    }

    public void run(int[] initialPeriods, int[] initialMedians, int[] initialSuperMedians) {
        this.initialMedians = initialMedians;
        this.initialPeriods = initialPeriods;
        this.initialSuperMedians = initialSuperMedians;
        run();
    }

    @Override
    protected void getResults(AMPL ampl) {
        Parameter n = ampl.getParameter("n");
        this.medians = getMedianLabels(ampl, n);
        this.superMedians = getSuperMedianLabels(ampl, n);
        this.periods = getCustomerPeriods(ampl, n);
        logResults();
    }

    private void logResults() {
        Map<Integer, Integer> medianCounts = getCounts(this.medians);
        Map<Integer, Integer> superMedianCounts = getCounts(this.superMedians);
        int[] customerSuperMedians = getCustomerSuperMedians(this.medians, this.superMedians);
        System.out.println("exact solution: (obj=" + this.objective + ", time=" + this.elapsedTime + ")");
        String title = "medians count";
        printCounts(medianCounts, title);
        String title2 = "supermedians count";
        printCounts(superMedianCounts, title2);
        printIndexes(medianCounts.keySet(), Arrays.stream(periods).boxed().collect(Collectors.toList()), "median periods");
        printIndexes(medianCounts.keySet(), Arrays.stream(superMedians).boxed().collect(Collectors.toList()), "medians' supermedians");
        Location[] locs = this.services.stream().map(Service::getLocation).toArray(Location[]::new);
        ZonesCSVWriter.write(this.resultPath, locs, this.periods, this.medians, customerSuperMedians);
    }

    private <T> void printIndexes(Collection<Integer> indexes, List<T> arr, String title) {
        System.out.print(title + ": (");
        String body = indexes.stream()
                .map(i -> new Pair(i, arr.get(i)))
                .map(p -> p.getFirst() + " => " + p.getSecond())
                .collect(Collectors.joining(", "));
        System.out.print(body);
        System.out.println(")");
    }

    @Override
    protected void loadParams(AMPL ampl) {
        Parameter nn = ampl.getParameter("n");
        nn.setValues(n);

        Parameter pp = ampl.getParameter("p");
        pp.setValues(p);

        Parameter mm = ampl.getParameter("m");
        mm.setValues(m);

        Parameter cc = ampl.getParameter("c");
        setMatrix(cc, c);

        Parameter rr = ampl.getParameter("r");
        rr.setValues(Arrays.stream(r).boxed().mapToDouble(Integer::doubleValue).toArray());

        Parameter dd = ampl.getParameter("d");
        dd.setValues(Arrays.stream(d).boxed().mapToDouble(Integer::doubleValue).toArray());

        Parameter aalpha = ampl.getParameter("alpha");
        aalpha.setValues(alpha);

        logModelParameters(ampl);
    }

    private void logModelParameters(final AMPL ampl) {
        System.out.println("exact params: (n=" + ampl.getParameter("n").getValues().getRowByIndex(0)[0] +
                ", p=" + ampl.getParameter("p").getValues().getRowByIndex(0)[0] +
                ", m=" + ampl.getParameter("m").getValues().getRowByIndex(0)[0] +
                ", alpha=" + ampl.getParameter("alpha").getValues().getRowByIndex(0)[0] + ")");
    }

    @Override
    protected void loadInitialSolution(AMPL ampl) {
        if (this.initialPeriods == null || this.initialPeriods.length != n || this.initialMedians == null ||
                this.initialMedians.length != n || this.initialSuperMedians == null || this.initialSuperMedians.length != n) {
            super.loadInitialSolution(ampl);
        } else {
            DataFrame xdf = new DataFrame(3, "N1", "N2", "T", "x");
            for (int i=0; i<n; i++) {
                int median = initialMedians[i];
                int period = initialPeriods[i];
                for (int j=0; j<n; j++) {
                    for (int t=0; t<m; t++) {
                        double xijt = (median == j && period == t) ? 1. : 0.;
                        xdf.addRow(i, j, t, xijt);
                    }
                }
            }
            Variable x = ampl.getVariable("x");
            x.setValues(xdf);

            DataFrame ydf = new DataFrame(2, "N1", "N2", "y");
            for (int i=0; i<n; i++) {
                for (int j=0; j<n; j++) {
                    int supermedian = initialSuperMedians[i];
                    if (supermedian == -1 || supermedian != j)
                        ydf.addRow(i,j,0.);
                    else
                        ydf.addRow(i,j,1.);
                }
            }
            Variable y = ampl.getVariable("y");
            y.setValues(ydf);

            DataFrame zdf = new DataFrame(3, "N1", "N2", "T", "z");
            for (int j=0; j<n; j++) {
                for (int k=0; k<n; k++) {
                    for (int t=0; t<m; t++) {
                        boolean xjjt = ((initialMedians[j] == j) && (initialPeriods[j] == t));
                        boolean yjk = ((initialSuperMedians[j] == k));
                        zdf.addRow(j, k, t, (xjjt && yjk) ? 1. : 0.);
                    }
                }
            }
            Variable z = ampl.getVariable("z");
            z.setValues(zdf);

            double xavg = (double) n / (p*m);
            DataFrame wdf = new DataFrame(1, "N", "w");
            int[] counts = new int[n];
            for (int i=0; i<n; i++) {
                counts[initialMedians[i]] += 1;
            }

            for (int i=0; i<n; i++) {
                int isMedian = (initialMedians[i] == i) ? 1 : 0;
                wdf.addRow(i, isMedian * Math.abs(counts[i] - xavg));
            }
            Variable w = ampl.getVariable("w");
            w.setValues(wdf);
        }

    }

    @Override
    protected void loadModel(AMPL ampl) throws IOException {
        super.loadModel(ampl);
    }

    private int[] getCustomerSuperMedians(int[] medianLabels, int[] superMedianLabels) {
        return Arrays.stream(medianLabels).map(i -> superMedianLabels[i]).toArray();
    }

    private int[] getSuperMedianLabels(final AMPL ampl, final Parameter n) {
        Variable x = ampl.getVariable("y");
        DataFrame df = x.getValues();
        int[] labels = new int[(int) Math.floor((double) n.get())];
        Arrays.fill(labels, -1);
        df.iterator().forEachRemaining(os -> {
            if ((double) os[2] == 1.)
                labels[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });
        return labels;
    }

    private int[] getCustomerPeriods(final AMPL ampl, final Parameter n) {
        Variable x = ampl.getVariable("x");
        DataFrame df = x.getValues();
        int[] periods = new int[(int) Math.floor((double) n.get())];
        df.iterator().forEachRemaining(os -> {
            if ((double) os[3] == 1.)
                periods[(int) Math.round((double) os[0])] = (int) Math.round((double) os[2]);
        });
        return periods;
    }

    private int[] getMedianLabels(final AMPL ampl, final Parameter n) {
        Variable x = ampl.getVariable("x");
        DataFrame df = x.getValues();
        int[] labels = new int[(int) Math.floor((double) n.get())];
        df.iterator().forEachRemaining(os -> {
            if ((double) os[3] == 1.)
                labels[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });
        return labels;
    }

    private void printCounts(final Map<Integer, Integer> counts, final String title) {
        System.out.print(title + ": (");
        boolean first = true;
        List<Integer> keys = counts.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer k : keys) {
            String s = first ? k + " => " + counts.get(k) : ", " + k + " => " + counts.get(k);
            first = false;
            System.out.print(s);
        }
        System.out.println(")");
    }

    private Map<Integer, Integer> getCounts(final int[] labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<labels.length; i++) {
            if (labels[i] == -1) continue;
            int cnt = counts.getOrDefault(labels[i], 0);
            counts.put(labels[i], cnt+1);
        }
        return counts;
    }
}
