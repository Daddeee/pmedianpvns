package it.polimi.algorithms.vrp.objectives;

import it.polimi.domain.Location;
import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;
import it.polimi.domain.routing.VehicleRoutingProblemSolution;

public class TotalDistance extends ObjectiveFunction {
    private final VehicleRoutingProblem vrp;

    public TotalDistance(VehicleRoutingProblem vrp, double unassignedPenalty) {
        super(unassignedPenalty);
        this.vrp = vrp;
    }

    @Override
    public double getCost(VehicleRoutingProblemSolution solution) {
        double cost = 0.;
        for (VehicleRoute route : solution.getRoutes()) {
            Location cur = vrp.getDepot().getLocation(), prev;
            for (Job job : route.getJobs()) {
                prev = cur;
                cur = job.getLocation();
                cost += vrp.getDistance().distance(prev, cur);
            }
            cost += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
        }
        cost += solution.getUnassignedJobs().size() * unassignedPenalty;
        return cost;
    }
}
