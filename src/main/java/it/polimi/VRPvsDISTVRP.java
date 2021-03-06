package it.polimi;

import it.polimi.algorithms.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithms.vrp.ruin.ZoneRemoval;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.distances.ZonePenalyzed;
import it.polimi.domain.Location;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.Vehicle;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;
import it.polimi.io.LatLngCSVReader;
import it.polimi.io.VRPWriter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VRPvsDISTVRP {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        String zonesPath = "instances/zones-vs.csv";
        String modelPath = "models/tdp/multi-period-balanced-p-median.mod";
        String instancePath = "instances/test.csv";
        int numVehicles = 5;
        int numPeriods = 3;

        /*TestCSVReader reader = new TestCSVReader();
        reader.readCSV(new File(instancePath));
        List<Service> services = reader.getServices();
        Location depot = reader.getDepot();
        List<Location> locations = services.stream().map(Service::getLocation).collect(Collectors.toList());
        Distance distance = new Euclidean(locations);

        MultiPeriodBalancedPMedianExact pMedianExact = new MultiPeriodBalancedPMedianExact(modelPath, zonesPath,
                services, distance, numVehicles, numPeriods);
        pMedianExact.run();*/

        List<Location> allLocations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        Location depot = new Location("depot",42.78614,11.09744);
        List<Location> locations = IntStream.range(0, allLocations.size())
                .mapToObj(i -> new Location("" + i, allLocations.get(i).getLat(), allLocations.get(i).getLng()))
                .collect(Collectors.toList());
        Distance distance = new Haversine(locations);
        System.out.println("Solving balanced pmedian with " + locations.size() + " points.");
        BalancedPMedianVNS vns = new BalancedPMedianVNS(locations.size(), numVehicles, distance.getDistancesMatrix());
        vns.run();
        System.out.println("Solved. obj=" + vns.getObjective());
        Map<Integer, List<Integer>> periodsMap = getPeriodsMap(IntStream.range(0, locations.size()).map(i -> 0).toArray());
        List<Integer> medians = Arrays.stream(vns.getLabels()).boxed().collect(Collectors.toList());

        //Map<Integer, List<Integer>> periodsMap = getPeriodsMap(pMedianExact.getPeriods());
        //List<Integer> medians = Arrays.stream(pMedianExact.getMedians()).boxed().collect(Collectors.toList());

        for (int period : periodsMap.keySet()) {
            System.out.println("Period " + period);
            List<Integer> idxs = periodsMap.get(period);
            List<Location> periodLocations = idxs.stream().map(locations::get).collect(Collectors.toList());
            List<Integer> periodMedians = idxs.stream().map(medians::get).collect(Collectors.toList());
            periodLocations.add(0, depot);
            periodMedians.add(0, -1);
            Distance dist = new Haversine(periodLocations);
            ZonePenalyzed penalyzed = new ZonePenalyzed(periodLocations, dist, 5., periodMedians);
            penalyzed.applyPenalty();

            Map<String, Integer> mediansMap = new HashMap<>();
            IntStream.range(0, periodLocations.size())
                    .forEach(i -> mediansMap.put(periodLocations.get(i).getId(), periodMedians.get(i)));

            VehicleRoutingProblem zonedProblem = getProblem(periodLocations, numVehicles, penalyzed);
            VehicleRoutingProblemSolution zonedSolution = VRP.solve(executorService, zonedProblem,
                    Collections.singletonList(new ZoneRemoval(mediansMap, zonedProblem, 0.4)), new ArrayList<>());
            VRPWriter.write("instances/results/routing/zoned/" + period + ".csv", zonedSolution, zonedProblem, mediansMap);

            VehicleRoutingProblem unzonedProblem = getProblem(periodLocations, numVehicles, dist);
            VehicleRoutingProblemSolution unzonedSolution = VRP.solve(executorService, unzonedProblem,
                    new ArrayList<>(), new ArrayList<>());
            VRPWriter.write("instances/results/routing/unzoned/" + period + ".csv", unzonedSolution, unzonedProblem);
        }
    }

    private static VehicleRoutingProblem getProblem(List<Location> locations, int numVehicles, Distance distance) {
        int size = 1;
        int capacity = (int) Math.floor(1.5 * locations.size() / numVehicles);
        Location depot = locations.get(0);
        Job depotJob = new Job(depot.getId(), depot, 0, 0);
        List<Job> jobs = IntStream.range(1, locations.size())
                .mapToObj(locations::get)
                .map(l -> new Job(l.getId(), l, size, 0.)).collect(Collectors.toList());
        List<Vehicle> vehicles = IntStream.range(0, numVehicles).mapToObj(i -> new Vehicle("" + i, capacity))
                .collect(Collectors.toList());
        return new VehicleRoutingProblem(depotJob, jobs, vehicles, distance);
    }

    public static Map<Integer, List<Integer>> getPeriodsMap(int[] periods) {
        Map<Integer, List<Integer>> periodsMap = new HashMap<>();
        for (int i=0; i<periods.length; i++) {
            List<Integer> services = periodsMap.getOrDefault(periods[i], new ArrayList<>());
            services.add(i);
            periodsMap.put(periods[i], services);
        }
        return periodsMap;
    }
}
