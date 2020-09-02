package it.polimi.algorithms.vrp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;
import it.polimi.utils.Rand;

import java.util.*;

public class ZoneRemoval implements RuinOperator {

    private Map<String, Integer> zonesMap;
    private VehicleRoutingProblem vrp;
    private double eps;
    private Random random;

    public ZoneRemoval(Map<String, Integer> zonesMap, final VehicleRoutingProblem vrp, final double eps) {
        this.zonesMap = zonesMap;
        this.vrp = vrp;
        this.eps = eps;
        this.random = new Random();
    }

    public ZoneRemoval(Map<String, Integer> zonesMap, final VehicleRoutingProblem vrp, final double eps, final int seed) {
        this.zonesMap = zonesMap;
        this.vrp = vrp;
        this.eps = eps;
        this.random = new Random(seed);
    }

    @Override
    public Solution ruin(Solution s) {
        VehicleRoutingProblemSolution vrps = (VehicleRoutingProblemSolution) s;
        List<Job> jobs = vrps.getRouteJobs();
        Map<Integer, List<Job>> solutionZonesMap = new HashMap<>();
        for (Job job : jobs) {
            int zone = this.zonesMap.get(job.getId());
            List<Job> zoneJobs = solutionZonesMap.getOrDefault(zone, new ArrayList<>());
            zoneJobs.add(job);
            solutionZonesMap.put(zone, zoneJobs);
        }

        List<Job> removable = new ArrayList<>();
        int removableLength = Integer.MAX_VALUE;
        for (int zone : solutionZonesMap.keySet()) {
            if (solutionZonesMap.get(zone).size() < removableLength) {
                removable = solutionZonesMap.get(zone);
                removableLength = solutionZonesMap.get(zone).size();
            }
        }

        int q = Math.min(1 + random.nextInt(Math.min(100, (int) Math.floor(eps*removable.size()))), removable.size());

        Set<Job> unassignedJobs = new HashSet<>();
        for (int i=0; i<q; i++) {
            Job toRemove = Rand.sampleAndRemove(removable, random);
            unassignedJobs.add(toRemove);
        }

        double cost = 0.;
        List<VehicleRoute> newVehicleRoutes = new ArrayList<>();
        for (VehicleRoute vr : vrps.getRoutes()) {
            List<Job> newJobs = new ArrayList<>();
            Location prev, cur = vrp.getDepot().getLocation();
            double time = 0, distance = 0;
            int size = 0;
            for (Job j : vr.getJobs()) {
                if (!unassignedJobs.contains(j)) {
                    prev = cur;
                    cur = j.getLocation();
                    time += vrp.getDistance().duration(prev, cur);
                    distance += vrp.getDistance().distance(prev, cur);
                    size += j.getSize();
                    cost += vrp.getDistance().distance(prev, cur);
                    newJobs.add(new Job(j.getId(), cur, j.getSize(), time));
                }
            }
            time += vrp.getDistance().duration(cur, vrp.getDepot().getLocation());
            distance += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
            cost += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
            newVehicleRoutes.add(new VehicleRoute(newJobs, vr.getVehicle(), time, distance, size));
        }
        List<Job> unassignedJobsList = new ArrayList<>(unassignedJobs);
        unassignedJobsList.addAll(vrps.getUnassignedJobs()); // fill with old unassigned
        cost += unassignedJobsList.size() * vrp.getObjectiveFunction().getUnassignedPenalty();
        //LOGGER.info("Jobs removed. New cost: " + cost);
        return new VehicleRoutingProblemSolution(newVehicleRoutes, unassignedJobsList, cost);
    }
}
