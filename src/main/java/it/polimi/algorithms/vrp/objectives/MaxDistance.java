package it.polimi.algorithms.vrp.objectives;

import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;

public class MaxDistance extends ObjectiveFunction {
    private final VehicleRoutingProblem vrp;

    public MaxDistance(VehicleRoutingProblem vrp, double unassignedPenalty) {
        super(unassignedPenalty);
        this.vrp = vrp;
    }

    @Override
    public double getCost(VehicleRoutingProblemSolution solution) {
        double maxcost = Double.MIN_VALUE;
        for (VehicleRoute route : solution.getRoutes()) {
            double cost = 0.;
            Location cur = vrp.getDepot().getLocation(), prev;
            for (Job job : route.getJobs()) {
                prev = cur;
                cur = job.getLocation();
                cost += vrp.getDistance().distance(prev, cur);
            }
            cost += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
            maxcost = Math.max(cost, maxcost);
        }
        maxcost += solution.getUnassignedJobs().size() * unassignedPenalty;
        return maxcost;
    }

    @Override
    public double getDelta(Job toInsert, Job prev, Job next, VehicleRoutingProblemSolution sol) {
        Distance d = vrp.getDistance();
        return d.distance(prev.getLocation(), toInsert.getLocation()) +
                d.distance(toInsert.getLocation(), next.getLocation()) -
                d.distance(prev.getLocation(), next.getLocation());
    }
}
