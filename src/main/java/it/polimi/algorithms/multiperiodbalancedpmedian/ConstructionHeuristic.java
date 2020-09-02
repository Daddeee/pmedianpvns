package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.algorithms.balancedpmedian.BalancedPMedianVNS;
import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.domain.Customer;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConstructionHeuristic {

    // params
    private int n;
    private int p;
    private int m;

    private List<Customer> customers;
    private Class<? extends Distance> distClazz;
    private int[] initPeriods;

    // results
    private int[] periods;
    private int[] medians;
    private int[] supermedians;

    public ConstructionHeuristic(List<Customer> customers, Class<? extends Distance> distClazz, int p, int m) {
        this.customers = customers;
        this.distClazz = distClazz;
        this.n = customers.size();
        this.p = p;
        this.m = m;
    }

    public ConstructionHeuristic(List<Customer> customers, Class<? extends Distance> distClazz, int p, int m,
                                 int[] initPeriods) {
        this.customers = customers;
        this.distClazz = distClazz;
        this.n = customers.size();
        this.p = p;
        this.m = m;
        this.initPeriods = initPeriods;
    }

    public int[] getPeriods() {
        return periods;
    }

    public int[] getMedians() {
        return medians;
    }

    public int[] getSupermedians() {
        return supermedians;
    }

    public void run() {
        this.periods = new int[n];
        this.medians = new int[n];
        this.supermedians = new int[n];
        Arrays.fill(supermedians, -1);

        Map<Integer, List<Integer>> perPeriodIndices = assignServicesToPeriods();

        for (int t : perPeriodIndices.keySet())
            solveSinglePeriodPMedian(t, perPeriodIndices.get(t));

        List<Integer> medianIndices = getIndicesOfEachMedian();

        int[][] medianPeriods = oneHotEncode(medianIndices.stream().map(i -> periods[i]).collect(Collectors.toList()), m);

        List<Location> periodicLocations = medianIndices.stream().map(customers::get).map(Customer::getLocation)
                .collect(Collectors.toList());

        Distance dist = getDistance(periodicLocations);
        HighLevelPMedian hpm = new HighLevelPMedian(periodicLocations.size(), p, m, dist.getDurationsMatrix(),
                medianPeriods);
        int[] x = hpm.run();
        for (int i=0; i<x.length; i++) {
            supermedians[medianIndices.get(i)] = medianIndices.get(x[i]);
        }
    }

    private Distance getDistance(List<Location> locations) {
        try {
            Constructor<? extends Distance> ctor = distClazz.getConstructor(List.class);
            return ctor.newInstance(locations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int[][] oneHotEncode(List<Integer> values, int ubValues) {
        int[][] encoded = new int[values.size()][ubValues];
        for (int i=0; i<encoded.length; i++)
            encoded[i][values.get(i)] = 1;
        return encoded;
    }

    private List<Integer> getIndicesOfEachMedian() {
        List<Integer> periodicMedianIndices = new ArrayList<>();
        for (int i=0; i<medians.length; i++) {
            if (medians[i] == i) {
                periodicMedianIndices.add(i);
            }
        }
        assert periodicMedianIndices.size() == m*p;
        return periodicMedianIndices;
    }

    private void solveSinglePeriodPMedian(int t, List<Integer> pindices) {
        List<Customer> pservices = pindices.stream().map(i -> customers.get(i)).collect(Collectors.toList());
        List<Location> plocations = pservices.stream().map(Customer::getLocation).collect(Collectors.toList());
        Distance dist = getDistance(plocations);
        BalancedPMedianVNS vns = new BalancedPMedianVNS(pservices.size(), p, dist.getDurationsMatrix());
        vns.run();
        int[] labels = vns.getLabels();
        assert labels.length == pindices.size();
        for (int i=0; i<labels.length; i++) {
            int idx = pindices.get(i);
            int label = pindices.get(labels[i]);
            periods[idx] = t;
            medians[idx] = label;
        }
    }

    private Map<Integer, List<Integer>> assignServicesToPeriods() {
        if (initPeriods == null || initPeriods.length != n)
            return buildPeriodsFromServices();
        else
           return buildPeriodsFromInitPeriods();
    }

    private Map<Integer, List<Integer>> buildPeriodsFromInitPeriods() {
        Map<Integer, List<Integer>> periodServices = new HashMap<>();
        for (int i=0; i<n; i++) {
            List<Integer> pserv = periodServices.getOrDefault(initPeriods[i], new ArrayList<>());
            pserv.add(i);
            periodServices.put(initPeriods[i], pserv);
        }
        return periodServices;
    }

    private Map<Integer, List<Integer>> buildPeriodsFromServices() {
        int[] idxs = argsort(customers.stream().mapToInt(Customer::getDays).toArray());
        int[] periodCounts = new int[m];
        Map<Integer, List<Integer>> periodServices = new HashMap<>();

        for (int i=0; i<n; i++) {
            int s = idxs[i];
            int r = customers.get(s).getReleaseDate();
            int d = Math.min(r + customers.get(s).getDays(), m) - 1;

            int min = Integer.MAX_VALUE, minIdx = -1;
            for (int j=r; j<= d; j++) {
                if (periodCounts[j] < min) {
                    min = periodCounts[j];
                    minIdx = j;
                }
            }

            if (minIdx != -1) {
                List<Integer> pserv = periodServices.getOrDefault(minIdx, new ArrayList<>());
                pserv.add(s);
                periodServices.put(minIdx, pserv);
                periodCounts[minIdx]++;
            }
        }
        return periodServices;
    }

    private int[] argsort(int[] arr) {
        return IntStream.range(0, arr.length)
                .boxed()
                .sorted(Comparator.comparingInt(i -> arr[i]))
                .mapToInt(i -> i)
                .toArray();
    }
}
