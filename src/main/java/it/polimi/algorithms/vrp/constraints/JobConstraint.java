package it.polimi.algorithms.vrp.constraints;

import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;

public interface JobConstraint {
    boolean isViolated(Job toInsert, Job prev, Job next, VehicleRoute route, VehicleRoutingProblem vrp);
}
