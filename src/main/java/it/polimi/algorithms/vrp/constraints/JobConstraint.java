package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;

public interface JobConstraint {
    boolean isViolated(Job toInsert, Job prev, Job next, VehicleRoute route, VehicleRoutingProblem vrp);
}
