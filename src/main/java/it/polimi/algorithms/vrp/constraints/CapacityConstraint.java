package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;

public class CapacityConstraint implements RouteConstraint {
    @Override
    public boolean isViolated(Job toInsert, VehicleRoute route, VehicleRoutingProblem vrp) {
        return toInsert.getSize() + route.getTotalSize() > route.getVehicle().getCapacity();
    }
}
