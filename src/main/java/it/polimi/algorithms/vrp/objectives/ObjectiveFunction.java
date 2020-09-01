package it.polimi.algorithms.vrp.objectives;

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
}
