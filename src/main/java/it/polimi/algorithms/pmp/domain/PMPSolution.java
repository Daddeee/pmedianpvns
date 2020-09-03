package it.polimi.algorithms.pmp.domain;

import it.polimi.domain.Location;
import it.polimi.domain.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PMPSolution implements Solution {
    private final List<Location> medians;
    private final List<Location> removed;
    private final Map<Location, Location> locationMedians;
    private final Map<Location, Location> locationBakcupMedians;
    private double cost;

    public PMPSolution(List<Location> medians, List<Location> removed, Map<Location, Location> locationMedians,
                       Map<Location, Location> locationBakcupMedians, double cost) {
        this.medians = medians;
        this.removed = removed;
        this.locationMedians = locationMedians;
        this.locationBakcupMedians = locationBakcupMedians;
        this.cost = cost;
    }

    public List<Location> getMedians() {
        return medians;
    }

    public List<Location> getRemoved() {
        return removed;
    }

    public Map<Location, Location> getLocationMedians() {
        return locationMedians;
    }

    public Map<Location, Location> getLocationBakcupMedians() {
        return locationBakcupMedians;
    }

    @Override
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public PMPSolution clone() {
        return new PMPSolution(new ArrayList<>(medians), new ArrayList<>(removed), new HashMap<>(locationMedians),
                new HashMap<>(locationBakcupMedians), cost);
    }
}
