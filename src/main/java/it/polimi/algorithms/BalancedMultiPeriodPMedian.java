package it.polimi.algorithms;

import com.ampl.*;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Service;
import it.polimi.io.SpeedyReader;
import it.polimi.io.ZonesCSVWriter;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BalancedMultiPeriodPMedian {
    public static void main(String[] args) {
        SpeedyReader reader = new SpeedyReader();
        Calendar startDate = getCalendar("13-12-2019", "dd-MM-yyyy");
        int p = 5;
        int t = 2;
        //List<Service> services = reader.read("Empoli", startDate, t);
        List<Service> services = reader.readCSV(new File("instances/speedy/grosseto-test.csv"));
        System.out.println("Number of services: " + services.size());
        runModel(services, p, t);
    }

    private static void runModel(final List<Service> services, final int p, final int t) {
        double[] releaseDates = getReleaseDates(services);
        double[] dueDates = getDueDates(releaseDates, services, t);

        AMPL ampl = new AMPL();
        try {
            ampl.read("models/tdp/time-balanced-p-median-absolute-value.mod");

            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=2");

            Parameter n = ampl.getParameter("n");
            n.setValues(services.size());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(p);

            Parameter tt = ampl.getParameter("t");
            tt.setValues(t);

            Parameter rr = ampl.getParameter("r");
            rr.setValues(releaseDates);

            Parameter dd = ampl.getParameter("d");
            dd.setValues(dueDates);

            Distance dist = new Haversine(services.stream().map(Service::getLocation).collect(Collectors.toList()));
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

            long start = System.nanoTime();
            ampl.solve();
            long end = System.nanoTime();

            double elapsed_time = (end - start) / 1e6;

            // Get the values of the variable Buy in a dataframe object

            Variable x = ampl.getVariable("x");
            DataFrame df = x.getValues();

            int[][] labels = new int[t][services.size()];
            for (int i=0; i<t; i++)
                Arrays.fill(labels[i], -1);

            df.iterator().forEachRemaining(os -> {
                if ((Double) os[3] == 1.)
                    labels[(int) Math.round((Double) os[2])][(int) Math.round((Double) os[1])] = (int) Math.round((Double) os[0]);
            });

            for (int j=0; j<t; j++) {
                final int[] periodLabels = labels[j];
                int rescaled = 0;
                Map<Integer, Integer> rescaleMap = new HashMap<>();
                for (int i=0; i<periodLabels.length; i++) {
                    if (periodLabels[i] == -1) continue;
                    if (!rescaleMap.containsKey(periodLabels[i])) {
                        rescaleMap.put(periodLabels[i], rescaled);
                        rescaled++;
                    }
                    periodLabels[i] = rescaleMap.get(periodLabels[i]);
                }

                Location[] locs = IntStream.range(0, services.size()).filter(i -> periodLabels[i] != -1)
                        .mapToObj(i -> services.get(i))
                        .map(Service::getLocation)
                        .toArray(Location[]::new);

                int[] filteredLabels = Arrays.stream(periodLabels).filter(l -> l != -1).toArray();

                ZonesCSVWriter.write("instances/results/exact/time-exact-" + j + ".csv", locs, filteredLabels);

                Map<Integer, Integer> counts = new HashMap<>();
                for (int i=0; i<filteredLabels.length; i++) {
                    int cnt = counts.getOrDefault(filteredLabels[i], 0);
                    counts.put(filteredLabels[i], cnt+1);
                }

                System.out.println("Period " + j);
                for (Integer k : counts.keySet())
                    System.out.println("Zone " + k + ": " + counts.get(k));
                System.out.println("------------------------------");
            }

            System.out.println("\nElapsed time [ms]: " + elapsed_time);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
    }

    private static double[] getReleaseDates(List<Service> services) {
        List<Long> releaseDates = services.stream()
                .map(Service::getReleaseDate)
                .map(Calendar::getTimeInMillis)
                .map(TimeUnit.MILLISECONDS::toDays)
                .collect(Collectors.toList());

        long min = releaseDates.stream().min(Long::compareTo).orElse(0L);

        return releaseDates.stream()
                .mapToDouble(rd -> rd - min)
                .toArray();
    }

    private static double[] getDueDates(final double[] releaseDates, final List<Service> services, final int t) {
        return IntStream.range(0, releaseDates.length)
                .mapToDouble(i -> Math.min(releaseDates[i] + services.get(i).getDays() - 1, t - 1))
                .toArray();
    }

    private static Calendar getCalendar(String sDate, String format) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            Date date = df.parse(sDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
