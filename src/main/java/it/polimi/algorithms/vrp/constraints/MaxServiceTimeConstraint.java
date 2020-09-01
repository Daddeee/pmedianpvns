package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;

public class MaxServiceTimeConstraint implements JobConstraint {
    private final double maxTime;
    private final VehicleRoutingProblem vrp;

    public MaxServiceTimeConstraint(final double maxTimeInSeconds, final VehicleRoutingProblem vrp) {
        this.maxTime = maxTimeInSeconds;
        this.vrp = vrp;
    }

    @Override
    public boolean isViolated(Job toInsert, Job prev, Job next, VehicleRoute route, VehicleRoutingProblem vrp) {
        double deltaTime = vrp.getDistance().duration(prev.getLocation(), toInsert.getLocation()) +
                vrp.getDistance().duration(toInsert.getLocation(), next.getLocation()) -
                vrp.getDistance().duration(prev.getLocation(), next.getLocation());

        return route.getEndTime() + deltaTime > maxTime;
    }
}
