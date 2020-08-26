package it.polimi.algorithms.multiperiodbalancedpmedian;

import asg.cliche.Param;
import com.ampl.*;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Service;
import it.polimi.io.SpeedyReader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiPeriodBalancedPMedianExact {
    public static void main( String[] args ) {
        SpeedyReader reader = new SpeedyReader();
        List<Service> services = reader.readCSV(new File("instances/speedy/grosseto-test.csv"));
        solve(services,3);
    }

    public static void solve(List<Service> services, int p) {
        List<Location> locations = services.stream().map(Service::getLocation).collect(Collectors.toList());
        List<Long> releaseDates = SpeedyReader.parseReleaseDates(services);
        List<Long> dueDates = SpeedyReader.parseDueDates(services);

        double nPeriods = dueDates.stream().mapToDouble(Long::doubleValue).max().orElse(0.);

        AMPL ampl = new AMPL();

        try {
            ampl.read("models/tdp/multi-period-balanced-p-median.mod");
            ampl.readData("");

            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=1 iisfind=1");

            Parameter n = ampl.getParameter("n");
            n.setValues(locations.size());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(p);

            Parameter m = ampl.getParameter("m");
            m.setValues(nPeriods);

            //Distance dist = new Euclidean(locations);
            Distance dist = new Haversine(locations);
            float[][] distMatrix = dist.getDurationsMatrix();
            Tuple[] tuples = new Tuple[distMatrix.length*distMatrix.length];
            double[] distances = new double[distMatrix.length*distMatrix.length];
            int count = 0;
            for (int i=0; i<distMatrix.length; i++) {
                for (int j=0; j<distMatrix[i].length; j++) {
                    tuples[count] = new Tuple(i, j);
                    distances[count] = distMatrix[i][j];
                    count++;
                }
            }

            Parameter c = ampl.getParameter("c");
            c.setValues(tuples, distances);

            Parameter alpha = ampl.getParameter("alpha");
            alpha.setValues(getAlpha(locations.size(), distMatrix));

            Parameter r = ampl.getParameter("r");
            r.setValues(releaseDates.stream().mapToDouble(Long::doubleValue).toArray());

            Parameter d = ampl.getParameter("d");
            d.setValues(dueDates.stream().mapToDouble(Long::doubleValue).toArray());

            logModelParameters(ampl);

            long start = System.nanoTime();

            ampl.solve();

            long end = System.nanoTime();

            double elapsed_time = (end - start) / 1e6;

            int[] medianLabels = getMedianLabels(ampl, n);
            Map<Integer, Integer> medianCounts = getCounts(medianLabels);

            int[] superMedianLabels = getSuperMedianLabels(ampl, n);
            Map<Integer, Integer> superMedianCounts = getCounts(superMedianLabels);

            double obj = ampl.getObjective("distance_and_displacement").value();
            System.out.println("exact solution: (obj=" + obj + ", time=" + elapsed_time + ")\n");

            String title = "medians";
            printCounts(medianCounts, title);

            String title2 = "supermedians";
            printCounts(superMedianCounts, title2);

            //Location[] locs = locations.toArray(new Location[0]);
            //ZonesCSVWriter.write("instances/results/balanced-exact.csv", locs, labels);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
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
        System.out.println(")\n");
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

    private static int[] getSuperMedianLabels(final AMPL ampl, final Parameter n) {
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

    private static int[] getMedianLabels(final AMPL ampl, final Parameter n) {
        Variable x = ampl.getVariable("x");
        DataFrame df = x.getValues();
        int[] labels = new int[(int) Math.floor((double) n.get())];
        df.iterator().forEachRemaining(os -> {
            if ((double) os[3] == 1.)
                labels[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });
        return labels;
    }

    private static void logModelParameters(final AMPL ampl) {
        System.out.println("exact params: (n=" + ampl.getParameter("n").getValues().getRowByIndex(0)[0] +
                ", p=" + ampl.getParameter("p").getValues().getRowByIndex(0)[0] +
                ", m=" + ampl.getParameter("m").getValues().getRowByIndex(0)[0] +
                ", alpha=" + ampl.getParameter("alpha").getValues().getRowByIndex(0)[0] + ")");
    }

    private static float getAlpha(final int n, final float[][] d) {
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += d[i][j];
                    count += 1;
                }
            }
        }
        return 0.2f * distSum / count;
    }
}
