package it.polimi.algorithms.vrp.recreate;

import it.polimi.domain.Location;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;

public class Insertion {
    public static final Insertion NO_INSERTION = new Insertion(null, null, -1, Double.MAX_VALUE, null);

    private Job job;
    private VehicleRoute vehicleRoute;
    private int position;
    private double delta;
    private VehicleRoutingProblem vrp;

    public Insertion(Job job, VehicleRoute vehicleRoute, int position, double delta, VehicleRoutingProblem vrp) {
        this.job = job;
        this.vehicleRoute = vehicleRoute;
        this.position = position;
        this.delta = delta;
        this.vrp = vrp;
    }

    public Job getJob() {
        return job;
    }

    public VehicleRoute getVehicleRoute() {
        return vehicleRoute;
    }

    public int getPosition() {
        return position;
    }

    public double getDelta() {
        return delta;
    }

    public boolean isBetterThan(Insertion ins) {
        return delta < ins.getDelta();
    }

    public void perform() {
        this.vehicleRoute.getJobs().add(this.position, this.job);
        Location prev = (this.position == 0)
                ? vrp.getDepot().getLocation()
                : this.vehicleRoute.getJobs().get(this.position - 1).getLocation();
        Location next = (this.position == this.vehicleRoute.getJobs().size() - 1)
                ? vrp.getDepot().getLocation()
                : this.vehicleRoute.getJobs().get(this.position + 1).getLocation();
        Location cur = this.job.getLocation();
        double deltaTime = vrp.getDistance().duration(prev, cur) + vrp.getDistance().duration(cur, next) - vrp.getDistance().duration(prev, next);
        double deltaDistance = vrp.getDistance().distance(prev, cur) + vrp.getDistance().distance(cur, next) - vrp.getDistance().distance(prev, next);
        this.vehicleRoute.setEndTime(this.vehicleRoute.getEndTime() + deltaTime);
        this.vehicleRoute.setTotalDistance(this.vehicleRoute.getTotalDistance() + deltaDistance);
        this.vehicleRoute.setTotalSize(this.vehicleRoute.getTotalSize() + this.job.getSize());
    }
}
