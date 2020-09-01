package it.polimi.algorithms.balancedpmedian;

import com.ampl.*;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Service;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.TestCSVReader;
import it.polimi.io.ZonesCSVWriter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BalancedPMedianExact {
    public static void main( String[] args ) {
        //List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        TestCSVReader reader = new TestCSVReader();
        List<Service> services = reader.readCSV(new File("instances/test.csv"));
        int p = 3;
        solve(services.stream().map(Service::getLocation).collect(Collectors.toList()), p);
    }

    public static void solve(List<Location> locations, int p) {
        AMPL ampl = new AMPL();

        try {
            ampl.read("models/tdp/balanced-p-median-absolute-value.mod");
            ampl.readData("");

            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=1");

            Parameter n = ampl.getParameter("n");
            n.setValues(locations.size());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(p);

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

            Parameter d = ampl.getParameter("d");
            d.setValues(tuples, distances);

            Parameter alpha = ampl.getParameter("alpha");
            alpha.setValues(BalancedPMedianVNS.getAlpha(locations.size(), distMatrix));



            System.out.println("exact params: (n=" + ampl.getParameter("n").getValues().getRowByIndex(0)[0] +
                    ", p=" + ampl.getParameter("p").getValues().getRowByIndex(0)[0] +
                    ", alpha=" + ampl.getParameter("alpha").getValues().getRowByIndex(0)[0] + ")");

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

            Map<Integer, Integer> counts = new HashMap<>();
            for (int i=0; i<labels.length; i++) {
                int c = counts.getOrDefault(labels[i], 0);
                counts.put(labels[i], c+1);
            }

            double obj = ampl.getObjective("distance_and_displacement").value();

            System.out.println("exact solution: (obj=" + obj + ", time=" + elapsed_time + ")");

            System.out.print("exact zones: (");

            boolean first = true;
            List<Integer> keys = counts.keySet().stream().sorted().collect(Collectors.toList());
            for (Integer k : keys) {
                String s = first ? k + " => " + counts.get(k) : ", " + k + " => " + counts.get(k);
                first = false;
                System.out.print(s);
            }

            System.out.println(")\n");

            Location[] locs = locations.toArray(new Location[0]);
            ZonesCSVWriter.write("instances/results/balanced-exact.csv", locs, labels);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
    }
}
