package it.polimi.domain.vrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VehicleRoute {
    private final List<Job> jobs;
    private final Vehicle vehicle;
    private double endTime;
    private double totalDistance;
    private int totalSize;

    public VehicleRoute(List<Job> jobs, Vehicle vehicle, double endTime, double totalDistance, int totalSize) {
        this.jobs = jobs;
        this.vehicle = vehicle;
        this.endTime = endTime;
        this.totalDistance = totalDistance;
        this.totalSize = totalSize;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public VehicleRoute clone() {
        return new VehicleRoute(new ArrayList<>(jobs), vehicle, endTime, totalDistance, totalSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleRoute that = (VehicleRoute) o;
        return Objects.equals(vehicle, that.vehicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicle);
    }
}
