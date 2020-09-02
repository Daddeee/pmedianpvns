package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;

public interface RouteConstraint {
    boolean isViolated(Job toInsert, VehicleRoute route, VehicleRoutingProblem vrp);
}
