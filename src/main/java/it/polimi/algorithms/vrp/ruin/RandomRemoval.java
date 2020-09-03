package it.polimi.algorithms.vrp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomRemoval implements RuinOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomRemoval.class);
    private final VehicleRoutingProblem vrp;
    private final double eps;
    private final Random random;

    public RandomRemoval(final VehicleRoutingProblem vrp, final double eps) {
        this.vrp = vrp;
        this.eps = eps;
        this.random = new Random();
    }

    public RandomRemoval(final VehicleRoutingProblem vrp, final double eps, int seed) {
        this.vrp = vrp;
        this.eps = eps;
        this.random = new Random(seed);
    }

    @Override
    public Solution ruin(Solution s) {
        VehicleRoutingProblemSolution vrps = (VehicleRoutingProblemSolution) s;
        List<Job> jobs = vrps.getRouteJobs();
        int q = 4 + random.nextInt(Math.min(96, (int) Math.floor(eps*(Math.max(jobs.size() - 4, 0)))));
        //LOGGER.info("Starting to remove " + q + " jobs.");
        Set<Job> unassignedJobs = new HashSet<>();
        for (int i=0; i<q; i++) {
            Job toRemove = Rand.sampleAndRemove(jobs, random);
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
        cost += unassignedJobsList.size() * vrp.getVRPObjectiveFunction().getUnassignedPenalty();
        //LOGGER.info("Jobs removed. New cost: " + cost);
        return new VehicleRoutingProblemSolution(newVehicleRoutes, unassignedJobsList, cost);
    }
}
