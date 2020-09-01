package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;

public interface RouteConstraint {
    boolean isViolated(Job toInsert, VehicleRoute route, VehicleRoutingProblem vrp);
}
