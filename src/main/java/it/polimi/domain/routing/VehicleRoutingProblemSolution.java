package it.polimi.domain.routing;

import it.polimi.domain.Location;
import it.polimi.domain.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleRoutingProblemSolution implements Solution {
    private final List<VehicleRoute> routes;
    private final List<Job> unassignedJobs;
    private double cost;

    public VehicleRoutingProblemSolution(List<VehicleRoute> routes, List<Job> unassignedJobs, double cost) {
        this.routes = routes;
        this.unassignedJobs = unassignedJobs;
        this.cost = cost;
    }

    public List<VehicleRoute> getRoutes() {
        return routes;
    }

    public List<Job> getUnassignedJobs() {
        return unassignedJobs;
    }

    @Override
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getNumJobs() {
        return routes.stream().mapToInt(vr -> vr.getJobs().size()).sum();
    }

    public List<Job> getRouteJobs() {
        return routes.stream().flatMap(vr -> vr.getJobs().stream()).collect(Collectors.toList());
    }

    public VehicleRoutingProblemSolution clone() {
        List<VehicleRoute> clonedRoutes = routes.stream().map(VehicleRoute::clone).collect(Collectors.toList());
        List<Job> clonedUnassigned = new ArrayList<>(unassignedJobs);
        return new VehicleRoutingProblemSolution(clonedRoutes, clonedUnassigned, cost);
    }
}
