package it.polimi.algorithms.vrp.objectives;

import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;

public abstract class VRPObjectiveFunction {

    protected final double unassignedPenalty;

    public VRPObjectiveFunction(double unassignedPenalty) {
        this.unassignedPenalty = unassignedPenalty;
    }

    public double getUnassignedPenalty() {
        return unassignedPenalty;
    }

    public abstract double getCost(VehicleRoutingProblemSolution solution);

    public abstract double getDelta(Job toInsert, Job prev, Job next, VehicleRoutingProblemSolution sol);
}
