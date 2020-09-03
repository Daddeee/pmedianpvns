package it.polimi.algorithms.vrp.objectives;

import it.polimi.distances.Distance;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;

public class TotalDistance extends VRPObjectiveFunction {
    private final VehicleRoutingProblem vrp;

    public TotalDistance(VehicleRoutingProblem vrp, double unassignedPenalty) {
        super(unassignedPenalty);
        this.vrp = vrp;
    }

    @Override
    public double getCost(VehicleRoutingProblemSolution solution) {
        double cost = 0.;
        for (VehicleRoute route : solution.getRoutes())
            cost += route.getTotalDistance();
        cost += solution.getUnassignedJobs().size() * unassignedPenalty;
        return cost;
    }

    @Override
    public double getDelta(Job toInsert, Job prev, Job next, VehicleRoutingProblemSolution sol) {
        Distance d = vrp.getDistance();
        return d.distance(prev.getLocation(), toInsert.getLocation()) +
                d.distance(toInsert.getLocation(), next.getLocation()) -
                d.distance(prev.getLocation(), next.getLocation());
    }
}
