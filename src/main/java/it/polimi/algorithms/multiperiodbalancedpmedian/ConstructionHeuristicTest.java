package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Customer;
import it.polimi.io.TestCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConstructionHeuristicTest {
    public static void main(String [] args) {
        //SpeedyReader reader = new SpeedyReader();
        //List<Service> services = reader.readCSV(new File("instances/speedy/grosseto-test.csv"));
        TestCSVReader reader = new TestCSVReader();
        reader.readCSV(new File("instances/test.csv"));
        List<Customer> customers = reader.getCustomers();
        List<Location> locations = customers.stream().map(Customer::getLocation).collect(Collectors.toList());
        Distance dist = new Haversine(locations);
        float[][] c = dist.getDurationsMatrix();
        int p = 4;
        int m = 3;
        ConstructionHeuristic ch = new ConstructionHeuristic(customers, Euclidean.class, p, m);
        long start = System.nanoTime();

        ch.run();

        long end = System.nanoTime();
        double elapsed_time = (end - start) / 1e6;
        double obj = computeObjectivefunction(customers.size(), p, m, ch.getMedians(), ch.getSupermedians(), c);
        int[] customerPeriods = ch.getPeriods();
        int[] customerMedians = ch.getMedians();
        int[] superMedians = ch.getSupermedians();
        int[] customerSuperMedians = IntStream.range(0, customers.size()).map(i -> superMedians[customerMedians[i]]).toArray();
        Map<Integer, Integer> medianCounts = getCounts(customerMedians);
        Map<Integer, Integer> superMedianCounts = getCounts(superMedians);

        System.out.println("exact solution: (obj=" + obj + ", time=" + elapsed_time + ")");
        String title = "medians";
        printCounts(medianCounts, title);
        String title2 = "supermedians";
        printCounts(superMedianCounts, title2);
        Location[] locs = locations.toArray(new Location[0]);
        ZonesCSVWriter.write("instances/test-res-ch.csv", locs, customerPeriods, customerMedians, customerSuperMedians);
    }

    private static void printCounts(final Map<Integer, Integer> counts, final String title) {
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

    private static Map<Integer, Integer> getCounts(final int[] labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<labels.length; i++) {
            if (labels[i] == -1) continue;
            int cnt = counts.getOrDefault(labels[i], 0);
            counts.put(labels[i], cnt+1);
        }
        return counts;
    }

    private static double computeObjectivefunction(int n, int p, int m, int[] medians, int[] superMedians, float[][] c) {
        double alpha = getAlpha(n, c);
        double xavg = (double)n/(p*m);
        double w = 0;

        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<n; i++) {
            int cnt = counts.getOrDefault(medians[i], 0);
            counts.put(medians[i], cnt + 1);
            w += c[i][medians[i]];

            if (superMedians[i] != -1)
                w += c[i][superMedians[i]];
        }

        for (int cnt : counts.values())
            w += alpha * Math.abs(cnt - xavg);

        return w;
    }

    private static float getAlpha(final int n, final float[][] c) {
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
}
