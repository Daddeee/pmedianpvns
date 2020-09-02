package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.domain.Customer;
import it.polimi.io.ZonesCSVWriter;
import it.polimi.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class MultiPeriodBalancedPMedianVNS {
    protected final int MAX_SOLUTION_CHANGES = 100;
    private final Logger LOGGER = LoggerFactory.getLogger(MultiPeriodBalancedPMedianVNS.class);
    private final Random random = new Random(1337);

    private String resultPath;
    private List<Customer> customers;
    private Class<? extends Distance> distClazz;

    // params
    private int n;
    private int p;
    private int m;
    private int[] r;
    private int[] d;
    private float[][] c;
    private double alpha;
    private double xavg;
    private int kmax;

    // solutions
    private int[] periods;
    private int[] medians;
    private int[] superMedians;
    private double objective;
    private double elapsedTime;

    public MultiPeriodBalancedPMedianVNS(String resultPath, List<Customer> customers, Class<? extends Distance> distClazz,
                                         int p, int m, int kmax) {
        this.resultPath = resultPath;
        this.customers = customers;
        this.distClazz = distClazz;
        this.n = customers.size();
        this.p = p;
        this.m = m;
        this.r = new int[n];
        this.d = new int[n];
        for (int i=0; i<n; i++) {
            Customer s = customers.get(i);
            r[i] = s.getReleaseDate();
            d[i] = Math.min(r[i] + s.getDays(), m) - 1;
        }
        Distance dist = getDistance(customers.stream().map(Customer::getLocation).collect(Collectors.toList()));
        this.c = dist.getDurationsMatrix();
        this.alpha = getAlpha(n, c);
        this.xavg = (double) n / (m*p);
        this.kmax = kmax;
    }

    public int[] getPeriods() {
        return periods;
    }

    public int[] getMedians() {
        return medians;
    }

    public int[] getSuperMedians() {
        return superMedians;
    }

    public double getObjective() {
        return objective;
    }

    public void run() {
        long start = System.nanoTime();
        ConstructionHeuristic ch = new ConstructionHeuristic(customers, distClazz, p, m);
        ch.run();

        int[] mediansCur = ch.getMedians();
        int[] superMediansCur = ch.getSupermedians();
        int[] periodsCur = ch.getPeriods();
        double fcur = computeObjectiveFunction(mediansCur, superMediansCur);

        int[] mediansOpt = mediansCur.clone();
        int[] superMediansOpt = superMediansCur.clone();
        int[] periodsOpt = periodsCur.clone();
        double fopt = fcur;

        int k = 1;
        int changes = 0;
        while (k <= kmax) {
            // shaking: swap periods of services
            for (int j = 1; j <= k; j++) {
                int swapin = randomServiceToSwap(periodsCur, r, d);
                int swapout = bestServiceToBeSwapped(swapin, periodsCur, r, d, mediansCur, superMediansCur);
                if (swapout == -1) {
                    System.out.println("Cannot swap " + swapin + ". Skipping.");
                    continue;
                }
                swap(periodsCur, swapin, swapout);
            }

            ch = new ConstructionHeuristic(customers, distClazz, p, m, periodsCur);
            ch.run();

            mediansCur = ch.getMedians();
            superMediansCur = ch.getSupermedians();
            periodsCur = ch.getPeriods();
            fcur = computeObjectiveFunction(mediansCur, superMediansCur);

            if (fcur < fopt) {
                fopt = fcur;
                mediansOpt = mediansCur.clone();
                superMediansOpt = superMediansCur.clone();
                periodsOpt = periodsCur.clone();
                k = 1;
                changes++;
                if (changes >= MAX_SOLUTION_CHANGES) {
                    LOGGER.info("Max solution changes limit hit.");
                    break;
                }
            } else {
                mediansCur = mediansOpt.clone();
                superMediansCur = superMediansOpt.clone();
                periodsCur = periodsOpt.clone();
                k = k + 1;
            }
        }
        long end = System.nanoTime();

        this.medians = mediansOpt;
        this.superMedians = superMediansOpt;
        this.periods = periodsOpt;
        this.objective = fopt;
        this.elapsedTime = (end - start) / 1e6;

        logResults();
    }

    private void swap(int[] arr, int i1, int i2) {
        int tmp = arr[i1];
        arr[i1] = arr[i2];
        arr[i2] = tmp;
    }

    private int randomServiceToSwap(int[] periods, int[] r, int[] d) {
        int x;
        do {
            x = random.nextInt(periods.length);
        } while (r[x] == d[x]);
        return x;
    }

    private int bestServiceToBeSwapped(int goin, int[] periods, int[] r, int[] d, int[] meds, int[] supermeds) {
        int goout = -1;
        double gooutCost = Integer.MIN_VALUE;
        for (int i=0; i<n; i++) {
            if (periods[i] != periods[goin] && canSwap(goin, i, periods, r, d)) {
                double cost = c[i][meds[i]]; // TODO consider also balance and supermedians in there ?
                if (cost > gooutCost) {
                    gooutCost = cost;
                    goout = i;
                }
            }
        }
        return goout;
    }

    private boolean canSwap(int i, int j, int[] periods, int[] r, int[] d) {
        return r[i] <= periods[j] && periods[j] <= d[i] &&
                r[j] <= periods[i] && periods[i] <= d[j];
    }

    private double computeObjectiveFunction(int[] medians, int[] supermedians) {
        double w = 0.;
        int n = medians.length;
        Map<Integer, Integer> counts = new HashMap<>();

        for (int i=0; i<n; i++) {
            w += c[i][medians[i]];

            if (supermedians[i] != -1)
                w += c[i][supermedians[i]];

            int c = counts.getOrDefault(medians[i], 0);
            counts.put(medians[i], c + 1);
        }

        if (counts.values().size() == 0)
            return w;

        for (int c : counts.values())
            w += alpha * Math.abs(c - xavg);

        return w;
    }

    private Distance getDistance(List<Location> periodicLocations) {
        try {
            Constructor<? extends Distance> ctor = distClazz.getConstructor(List.class);
            return ctor.newInstance(periodicLocations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private float getAlpha(final int n, final float[][] c) {
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
        Location[] locs = this.customers.stream().map(Customer::getLocation).toArray(Location[]::new);
        ZonesCSVWriter.write(this.resultPath, locs, this.periods, this.medians, customerSuperMedians);
    }

    private int[] getCustomerSuperMedians(int[] medianLabels, int[] superMedianLabels) {
        return Arrays.stream(medianLabels).map(i -> superMedianLabels[i]).toArray();
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

    private <T> void printIndexes(Collection<Integer> indexes, List<T> arr, String title) {
        System.out.print(title + ": (");
        String body = indexes.stream()
                .map(i -> new Pair(i, arr.get(i)))
                .map(p -> p.getFirst() + " => " + p.getSecond())
                .collect(Collectors.joining(", "));
        System.out.print(body);
        System.out.println(")");
    }
}
