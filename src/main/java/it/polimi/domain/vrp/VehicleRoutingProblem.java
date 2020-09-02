package it.polimi.domain.vrp;

import it.polimi.algorithms.vrp.objectives.ObjectiveFunction;
import it.polimi.algorithms.vrp.constraints.JobConstraint;
import it.polimi.algorithms.vrp.constraints.RouteConstraint;
import it.polimi.distances.Distance;
import it.polimi.domain.Problem;

import java.util.ArrayList;
import java.util.List;

public class VehicleRoutingProblem implements Problem {
    private Job depot;
    private List<Job> jobs;
    private List<Vehicle> vehicles;
    private Distance distance;

    private ObjectiveFunction objectiveFunction;
    private List<JobConstraint> jobConstraints;
    private List<RouteConstraint> routeConstraints;

    public VehicleRoutingProblem(Job depot, List<Job> jobs, List<Vehicle> vehicles, Distance distance) {
        this.depot = depot;
        this.jobs = jobs;
        this.vehicles = vehicles;
        this.distance = distance;
        this.jobConstraints = new ArrayList<>();
        this.routeConstraints = new ArrayList<>();
    }

    public Job getDepot() {
        return depot;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Distance getDistance() {
        return distance;
    }

    public List<JobConstraint> getJobConstraints() {
        return jobConstraints;
    }

    public List<RouteConstraint> getRouteConstraints() {
        return routeConstraints;
    }

    public ObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(ObjectiveFunction objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public void addConstraint(JobConstraint jobConstraint) {
        this.jobConstraints.add(jobConstraint);
    }

    public void addConstraint(RouteConstraint routeConstraint) {
        this.routeConstraints.add(routeConstraint);
    }
}
