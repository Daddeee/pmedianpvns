package it.polimi.benchmarks;

import it.polimi.algorithms.pmedian.PMedianVNS;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Speedy {
    public static void main(String[] args) {
        int p = 7;
        List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-12.csv");
        Distance dist = new Haversine(locations);

        long start = System.nanoTime();
        PMedianVNS vns = new PMedianVNS(locations.size(), p, dist.getDurationsMatrix());
        vns.run();
        long end = System.nanoTime();

        double time = (end - start) / 1e6;

        System.out.println("obj:  " + vns.getObjective());
        System.out.println("time: " + time);

        int[] labels = vns.getLabels();
        labels = encode(labels);

        int[] counts = new int[p];
        for (int i=0; i<labels.length; i++)
            counts[labels[i]] += 1;

        System.out.println("zones data:");
        for (int i=0; i<counts.length; i++)
            System.out.println("zone " + i + ": " + counts[i]);

        ZonesCSVWriter.write("instances/results/vns/zones.csv", locations.toArray(new Location[0]), labels);
    }

    private static int[] encode (int[] arr) {
        int count = 0;
        int[] encoded = new int[arr.length];
        Map<Integer, Integer> encodeMap = new HashMap<>();
        for (int i=0; i<arr.length; i++) {
            if (!encodeMap.containsKey(arr[i])) {
                encodeMap.put(arr[i], count);
                count++;
            }
            encoded[i] = encodeMap.get(arr[i]);
        }
        return encoded;
    }
}
