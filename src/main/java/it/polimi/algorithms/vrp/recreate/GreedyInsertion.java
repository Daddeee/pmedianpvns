package it.polimi.algorithms.vrp.recreate;

import it.polimi.algorithms.alns.RecreateOperator;
import it.polimi.algorithms.vrp.constraints.JobConstraint;
import it.polimi.algorithms.vrp.constraints.RouteConstraint;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.domain.routing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class GreedyInsertion implements RecreateOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreedyInsertion.class);
    private VehicleRoutingProblem vrp;
    private ExecutorService executorService;

    public GreedyInsertion(VehicleRoutingProblem vrp, ExecutorService executorService) {
        this.vrp = vrp;
        this.executorService = executorService;
    }

    @Override
    public Solution recreate(Solution s) {
        VehicleRoutingProblemSolution vrps = ((VehicleRoutingProblemSolution) s).clone();
        int insertionsCount = 0;
        Map<VehicleRoute, Insertion> bestInsertions = new HashMap<>();
        //LOGGER.info("Inserting " + vrps.getUnassignedJobs().size() + " jobs.");
        while(vrps.getUnassignedJobs().size() > 0) {
            for (VehicleRoute vehicleRoute : vrps.getRoutes()) {
                // skip if there's no need to recalculate
                if (bestInsertions.containsKey(vehicleRoute)) continue;
                Insertion bestInsertion = Insertion.NO_INSERTION;
                List<Job> routeJobs = vehicleRoute.getJobs();

                for (Job toInsert : vrps.getUnassignedJobs()) {

                    boolean satisfiesRouteConstraints = true;
                    for (RouteConstraint routeConstraint : vrp.getRouteConstraints()) {
                        if (routeConstraint.isViolated(toInsert, vehicleRoute, vrp)) {
                            satisfiesRouteConstraints = false;
                            break;
                        }
                    }
                    if (!satisfiesRouteConstraints)
                        continue;

                    Job prev, next = vrp.getDepot();
                    Job cur = toInsert;
                    for (int i=0; i<routeJobs.size(); i++) {
                        prev = next;
                        next = routeJobs.get(i);

                        boolean satisfiesJobConstraints = true;
                        for (JobConstraint jobConstraint : vrp.getJobConstraints()) {
                            if (jobConstraint.isViolated(cur, prev, next, vehicleRoute, vrp)) {
                                satisfiesJobConstraints = false;
                                break;
                            }
                        }
                        if (!satisfiesJobConstraints)
                            continue;

                        double delta = getInsertionDelta(prev, next, cur);

                        Insertion insertion = new Insertion(cur, vehicleRoute, i, delta, vrp);
                        if (insertion.isBetterThan(bestInsertion))
                            bestInsertion = insertion;
                    }
                    prev = next;
                    next = vrp.getDepot();
                    double delta = getInsertionDelta(prev, next, cur);

                    boolean satisfiesJobConstraints = true;
                    for (JobConstraint jobConstraint : vrp.getJobConstraints()) {
                        if (jobConstraint.isViolated(cur, prev, next, vehicleRoute, vrp)) {
                            satisfiesJobConstraints = false;
                            break;
                        }
                    }
                    if (satisfiesJobConstraints) {
                        Insertion insertion = new Insertion(toInsert, vehicleRoute, routeJobs.size(), delta, vrp);
                        if (insertion.isBetterThan(bestInsertion))
                            bestInsertion = insertion;
                    }
                }

                bestInsertions.put(vehicleRoute, bestInsertion);
            }

            Insertion bestInsertionOverall = bestInsertions.values().stream()
                    .min(Comparator.comparingDouble(Insertion::getDelta)).orElse(Insertion.NO_INSERTION);

            if (bestInsertionOverall == Insertion.NO_INSERTION) {
                //LOGGER.info("Insertions incomplete. Inserted " + insertionsCount + " jobs.");
                return vrps;
            }

            /*LOGGER.info("Inserting job " + bestInsertionOverall.getJob().getId() + " in route " +
                    bestInsertionOverall.getVehicleRoute().getVehicle().getId() + ", capacity=" +
                    bestInsertionOverall.getVehicleRoute().getVehicle().getCapacity() + ", size=" +
                    bestInsertionOverall.getVehicleRoute().getTotalSize());*/
            double insertionCost = bestInsertionOverall.perform();
            //LOGGER.info("Inserted job. New size: " + bestInsertionOverall.getVehicleRoute().getTotalSize());
            insertionsCount++;
            vrps.setCost(vrps.getCost() + insertionCost - vrp.getObjectiveFunction().getUnassignedPenalty());
            vrps.getUnassignedJobs().remove(bestInsertionOverall.getJob());
            for (VehicleRoute route : vrps.getRoutes()) {
                if (bestInsertions.containsKey(route) &&
                        bestInsertions.get(route) != Insertion.NO_INSERTION &&
                        bestInsertions.get(route).getJob().equals(bestInsertionOverall.getJob())) {
                    bestInsertions.remove(route);
                }
            }
        }
        //LOGGER.info("Insertions complete. Inserted " + insertionsCount + " jobs.");
        return vrps;
    }

    protected double getInsertionDelta(Job prev, Job next, Job cur) {
        return dist(prev, cur) + dist(cur, next) - dist(prev, next);
    }

    private double dist(Job j1, Job j2) {
        return this.vrp.getDistance().distance(j1.getLocation(), j2.getLocation());
    }
}
