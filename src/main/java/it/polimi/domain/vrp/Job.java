package it.polimi.domain.vrp;

import it.polimi.domain.Location;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Job {
    private String id;
    private Location location;
    private int size;
    private Set<String> skills;

    private double arrTime;

    public Job(String id, Location location, int size, double arrTime) {
        this.id = id;
        this.location = location;
        this.size = size;
        this.arrTime = arrTime;
        this.skills = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public int getSize() {
        return size;
    }

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public boolean hasSkill(String skill) {
        return this.skills.contains(skill);
    }

    public double getArrTime() {
        return arrTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return id.equals(job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
