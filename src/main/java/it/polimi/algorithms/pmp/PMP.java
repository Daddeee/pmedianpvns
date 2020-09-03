package it.polimi.algorithms.pmp;

import it.polimi.algorithms.alns.*;
import it.polimi.algorithms.pmp.objectives.BalancedTotalDistance;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.algorithms.pmp.domain.PMPSolution;
import it.polimi.algorithms.pmp.domain.PMProblem;
import it.polimi.io.LatLngCSVReader;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PMP {
    private static final Logger LOGGER = LoggerFactory.getLogger(PMP.class);
    public static void main( String[] args ) {
        List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        int p = 5;
        /*List<Location> locations = new ArrayList<>();
        Random random = new Random(10);
        for (int i=0; i<10; i++) {
            Location l = new Location("" + i, random.nextInt(20), random.nextInt(20));
            locations.add(l);
            System.out.println(i + ": " + "(" + l.getLat() + ", " + l.getLng() + ")");
        }
        solve(locations, 5);*/
        Distance dist = new Haversine(locations);
        double alpha = getAlpha(dist.getDistancesMatrix());
        double avg = (double) locations.size()/p;
        BalancedTotalDistance btd = new BalancedTotalDistance(1e6, dist, p, avg, alpha);
        PMProblem pmp = new PMProblem(locations, dist, p, btd);

        double costSum = 0.;
        int tries = 10;
        for (int i=0; i<tries; i++)
            costSum += solve(locations, p, alpha, pmp);
        System.out.println("FINAL AVG COST = " + costSum/tries);
    }

    private static double solve(List<Location> locations, int p, double alpha, PMProblem pmp) {
        double start = System.nanoTime();

        PMPSolution initial = getRandomInitial(pmp, new Random());
        LOGGER.info("Initial solution, obj=" + initial.getCost());
        MultipleStopping stopping = new MultipleStopping(Arrays.asList(new MaxIterationStopping(50),
                new EarlyStopping(10)));
        OperatorsSelector operatorsSelector = new OperatorsSelector(30, 15, 10, 0.1);
        TemperatureAcceptance acceptance = new TemperatureAcceptance(getTStart(pmp, initial), 0.99);

        double maxN = getMaxN(pmp.getDistance().getDistancesMatrix());
        PMPALNS pmpalns = new PMPALNS(maxN, 100, stopping, operatorsSelector, acceptance);
        PMPSolution solution = (PMPSolution) pmpalns.run(pmp, initial);

        double end = System.nanoTime();
        double elapsedTimeMs = (end - start) / 1e6;

        System.out.println("alns params: (n=" + locations.size() + ", p=" + p + ", alpha=" + alpha + ")");
        System.out.println("alns solution: (obj=" + solution.getCost() + ", time=" + elapsedTimeMs + ")");

        Map<String, Integer> counts = new HashMap<>();
        for (Location key : solution.getLocationMedians().keySet()) {
            Location median = solution.getLocationMedians().get(key);
            counts.put(median.getId(), counts.getOrDefault(median.getId(), 0) + 1);
        }
        boolean first = true;
        System.out.print("alns zones: (");
        for (String key : counts.keySet()) {
            if (first) {
                first = false;
                System.out.print(key + " => " + counts.get(key));
            } else {
                System.out.print("," + key + " => " + counts.get(key));
            }
        }
        System.out.println(")");
        return solution.getCost();
    }

    private static PMPSolution getRandomInitial(PMProblem pmp, Random random) {
        List<Location> locations = new ArrayList<>(pmp.getLocations());
        List<Location> medians = new ArrayList<>();
        for (int i=0; i<pmp.getNumMedians(); i++)
            medians.add(Rand.sampleAndRemove(locations, random));

        Map<Location, Location> mediansMap = new HashMap<>(), backupMediansMap = new HashMap<>();
        for (Location location : pmp.getLocations()) {
            Location bestMedian = null, secondBestMedian = null;
            double bestDistance = Double.MAX_VALUE, secondBestDistance = Double.MAX_VALUE;
            for (Location median : medians) {
                double distance = pmp.getDistance().distance(location, median);
                if (distance < bestDistance) {
                    secondBestDistance = bestDistance;
                    bestDistance = distance;
                    secondBestMedian = bestMedian;
                    bestMedian = median;
                } else if (distance < secondBestDistance) {
                    secondBestDistance = distance;
                    secondBestMedian = median;
                }
            }
            mediansMap.put(location, bestMedian);
            backupMediansMap.put(location, secondBestMedian);
        }
        PMPSolution initial = new PMPSolution(medians, new ArrayList<>(), mediansMap, backupMediansMap, 0);
        double cost = pmp.getObjectiveFunction().getCost(initial);
        initial.setCost(cost);
        return initial;
    }

    private static double getMaxN(final float[][] d) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                if (d[i][j] > max)
                    max = d[i][j];
            }
        }
        return 0.3*max;
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

    private static double getTStart(PMProblem pmp, PMPSolution pmps) {
        double cost = pmp.getObjectiveFunction().getCost(pmps);
        double w = 0.05;
        return w*cost / Math.log(2);
    }
}
