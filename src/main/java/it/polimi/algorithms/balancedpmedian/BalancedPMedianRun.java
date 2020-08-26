package it.polimi.algorithms.balancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BalancedPMedianRun {
    public static void main( String[] args ) {
        List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        int p = 6;
        solve(locations, p);
    }

    public static void solve(final List<Location> locations, final int p) {
        Distance dist = new Haversine(locations);
        float[][] d = dist.getDurationsMatrix();

        //PMedianVNS vns = new PMedianVNS(locations.size(), p, d);
        BalancedPMedianVNS vns = new BalancedPMedianVNS(locations.size(), p, d);

        System.out.println("vns params: (n=" + locations.size() + ", p=" + p + ", alpha=" + vns.getAlpha() + ")");

        long start = System.nanoTime();
        vns.run();
        long end = System.nanoTime();

        double elapsed_time = (end - start) / 1e6;

        Map<Integer, Integer> counts = new HashMap<>();
        int[] labels = vns.getLabels();
        for (int i=0; i<labels.length; i++) {
            int c = counts.getOrDefault(labels[i], 0);
            counts.put(labels[i], c+1);
        }

        System.out.println("vns solution: (obj=" + vns.getObjective() + ", time=" + elapsed_time + ")");

        System.out.print("vns zones: (");
        boolean first = true;
        List<Integer> keys = counts.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer k : keys) {
            String s = first ? k + " => " + counts.get(k) : ", " + k + " => " + counts.get(k);
            first = false;
            System.out.print(s);
        }

        System.out.println(")\n");

        Location[] locs = locations.toArray(new Location[0]);
        ZonesCSVWriter.write("instances/results/balanced-vns.csv", locs, vns.getLabels());
    }
}
