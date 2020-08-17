package it.polimi.algorithm;

import com.ampl.*;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.util.*;
import java.util.stream.Collectors;

public class Exact {
    public static void main( String[] args ) {
        List<Location> latlngs = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        solve(latlngs, 5);
    }

    private static void solve(List<Location> locations, int p) {
        AMPL ampl = new AMPL();
        try {
            ampl.read("models/tdp/pmedian-bal.mod");
            ampl.readData("");

            ampl.setOption("solver", "cplex");

            Parameter n = ampl.getParameter("n");
            n.setValues(locations.size());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(p);


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

            Parameter d = ampl.getParameter("d");
            d.setValues(tuples, distances);

            long start = System.nanoTime();
            ampl.solve();
            long end = System.nanoTime();

            double elapsed_time = (end - start) / 1e6;

            // Get the values of the variable Buy in a dataframe object
            Variable x = ampl.getVariable("x");
            DataFrame df = x.getValues();

            int[] labels = new int[locations.size()];
            df.iterator().forEachRemaining(os -> {
                if ((double) os[2] == 1.)
                    labels[(int) Math.round((double) os[1])] = (int) Math.round((double) os[0]);
            });

            int rescaled = 0;
            Map<Integer, Integer> rescaleMap = new HashMap<>();
            for (int i=0; i<labels.length; i++) {
                if (!rescaleMap.containsKey(labels[i])) {
                    rescaleMap.put(labels[i], rescaled);
                    rescaled++;
                }
                labels[i] = rescaleMap.get(labels[i]);
            }

            Location[] locs = locations.toArray(new Location[0]);

            ZonesCSVWriter.write("instances/results/exact/pmedian-bal-1.csv", locs, labels);

            Map<Integer, Integer> counts = new HashMap<>();
            for (int i=0; i<labels.length; i++) {
                int c = counts.getOrDefault(labels[i], 0);
                counts.put(labels[i], c+1);
            }

            for (Integer k : counts.keySet())
                System.out.println("Zone " + k + ": " + counts.get(k));

            System.out.println("\ntime: " + elapsed_time);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
    }

}
