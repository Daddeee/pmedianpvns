package it.polimi.domain.mpbpmp;

import it.polimi.domain.Solution;

import java.util.List;
import java.util.Map;

public class MPBPMPSolution implements Solution {
    private Map<Service, Integer> periods;
    private Map<Service, Service> medians;
    private Map<Service, Service> supermedians;
    private List<Service> unassigned;
    private double cost;

    public MPBPMPSolution(Map<Service, Integer> periods, Map<Service, Service> medians, Map<Service,
            Service> supermedians, List<Service> unassigned, double cost) {
        this.periods = periods;
        this.medians = medians;
        this.supermedians = supermedians;
        this.unassigned = unassigned;
        this.cost = cost;
    }

    public Map<Service, Integer> getPeriods() {
        return periods;
    }

    public Map<Service, Service> getMedians() {
        return medians;
    }

    public Map<Service, Service> getSupermedians() {
        return supermedians;
    }

    public List<Service> getUnassigned() {
        return unassigned;
    }

    @Override
    public double getCost() {
        return cost;
    }
}
