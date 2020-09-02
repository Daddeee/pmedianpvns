package it.polimi.algorithms.vrp.objectives;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoutingProblem;
import it.polimi.domain.routing.VehicleRoutingProblemSolution;

public abstract class ObjectiveFunction {

    protected final double unassignedPenalty;

    public ObjectiveFunction(double unassignedPenalty) {
        this.unassignedPenalty = unassignedPenalty;
    }

    public double getUnassignedPenalty() {
        return unassignedPenalty;
    }

    public abstract double getCost(VehicleRoutingProblemSolution solution);

    public abstract double getDelta(Job toInsert, Job prev, Job next, VehicleRoutingProblemSolution sol);
}
