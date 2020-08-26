package it.polimi.algorithms.mostuniformpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostUniformPMedianRun {
    public static void main( String[] args ) {
        List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        int p = 6;
        solve(locations, p);
    }

    public static void solve(final List<Location> locations, final int p) {
        Distance dist = new Haversine(locations);
        float[][] d = dist.getDurationsMatrix();
        float alpha = getAlpha(d);

        //PMedianVNS vns = new PMedianVNS(locations.size(), p, d);
        MostUniformPMedianVNS vns = new MostUniformPMedianVNS(locations.size(), p, d);

        System.out.println("Solving with");
        System.out.println("n: " + locations.size());
        System.out.println("p: " + p);
        System.out.println("alpha: " +  alpha);

        vns.run();

        System.out.print("Medians: ");
        int[] medians = vns.getMedians();
        for (int i=0; i<medians.length; i++) {
            System.out.print(medians[i]);
            if (i < medians.length - 1) {
                System.out.print(", ");
            } else {
                System.out.println();
            }
        }

        Map<Integer, Integer> counts = new HashMap<>();
        int[] labels = vns.getLabels();
        for (int i=0; i<labels.length; i++) {
            int c = counts.getOrDefault(labels[i], 0);
            counts.put(labels[i], c+1);
            //System.out.println("Customer " + i + ":\t" + labels[i]);
        }

        for (Integer i : counts.keySet()) {
            System.out.println("Zone " + i + ": " + counts.get(i));
        }

        System.out.println("Objective: " + vns.getObjective());

        ZonesCSVWriter.write("instances/results/test.csv", locations.toArray(new Location[0]), vns.getLabels());
    }

    private static float getAlpha(final float[][] d) {
        float sum = 0;
        int count = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                sum += d[i][j];
                count++;
            }
        }
        return 0.2f*sum/count;
    }
}
