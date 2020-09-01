package it.polimi.algorithms.vrp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;
import it.polimi.domain.routing.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class WorstRemoval implements RuinOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorstRemoval.class);
    private final VehicleRoutingProblem vrp;
    private final int p;
    private final double eps;
    private final Random random;

    public WorstRemoval(VehicleRoutingProblem vrp, int p, double eps) {
        this.vrp = vrp;
        this.p = p;
        this.eps = eps;
        this.random = new Random();
    }

    public WorstRemoval(VehicleRoutingProblem vrp, int p, double eps, int seed) {
        this.vrp = vrp;
        this.p = p;
        this.eps = eps;
        this.random = new Random(seed);
    }

    @Override
    public Solution ruin(Solution s) {
        VehicleRoutingProblemSolution vrps = (VehicleRoutingProblemSolution) s;
        VehicleRoutingProblemSolution cloned = vrps.clone();
        int q = 4 + random.nextInt(Math.min(100, (int) Math.floor(eps*vrps.getNumJobs())) - 4);
        //LOGGER.info("Starting to remove " + q + " jobs.");
        double cost = cloned.getCost();
        for (int i=0; i<q; i++) {
            Job worst = getWorst(cloned);
            for (VehicleRoute vehicleRoute : cloned.getRoutes()) {
                double time = vehicleRoute.getEndTime();
                int idx = vehicleRoute.getJobs().indexOf(worst);
                if (idx >= 0) {
                    Location prev = (idx == 0)
                            ? vrp.getDepot().getLocation()
                            : vehicleRoute.getJobs().get(idx-1).getLocation();
                    Location next = (idx == vehicleRoute.getJobs().size() - 1)
                            ? vrp.getDepot().getLocation()
                            : vehicleRoute.getJobs().get(idx+1).getLocation();
                    Location cur = worst.getLocation();
                    cost += vrp.getDistance().distance(prev, next) - vrp.getDistance().distance(prev, cur) -
                            vrp.getDistance().distance(cur, next);
                    time += vrp.getDistance().duration(prev, next) - vrp.getDistance().duration(prev, cur) -
                            vrp.getDistance().duration(cur, next);
                    int size = vehicleRoute.getTotalSize() - worst.getSize();
                    Job removed = vehicleRoute.getJobs().remove(idx);
                    cloned.getUnassignedJobs().add(removed);
                    vehicleRoute.setEndTime(time);
                    vehicleRoute.setTotalSize(size);
                }
            }
        }
        cost += cloned.getUnassignedJobs().size() * vrp.getObjectiveFunction().getUnassignedPenalty();
        //LOGGER.info("Jobs removed. New cost: " + cost);
        cloned.setCost(cost);
        return cloned;
    }

    private Job getWorst(VehicleRoutingProblemSolution vrps) {
        Map<Job, Double> jobDeltas = new HashMap<>();
        for (VehicleRoute vr : vrps.getRoutes()) {
            Location prev, next, cur = vrp.getDepot().getLocation();
            for (int i=0; i<vr.getJobs().size(); i++) {
                Job toEvaluate = vr.getJobs().get(i);
                prev = cur;
                cur = toEvaluate.getLocation();
                next = (i >= vr.getJobs().size() - 1) ? vrp.getDepot().getLocation() : vr.getJobs().get(i+1).getLocation();
                Distance d = vrp.getDistance();
                double delta = d.distance(prev, next) - d.distance(prev, cur) - d.distance(cur, next);
                jobDeltas.put(toEvaluate, delta);
            }
        }
        int idx = (int) Math.floor(Math.pow(random.nextDouble(), p)*jobDeltas.keySet().size());
        if (idx == jobDeltas.keySet().size()) idx -= 1; // strange case
        List<Map.Entry<Job, Double>> jobs = jobDeltas.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Double::compareTo))
                .collect(Collectors.toList());
        return jobs.get(idx).getKey();
    }
}
