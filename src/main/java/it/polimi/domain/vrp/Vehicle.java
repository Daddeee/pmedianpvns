package it.polimi.domain.vrp;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Vehicle {
    private String id;
    private Set<String> skills;
    private int capacity;

    public Vehicle(String id, int capacity) {
        this.id = id;
        this.skills = new HashSet<>();
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void add(String skill) {
        this.skills.add(skill);
    }

    public boolean hasSkill(String skill) {
        return this.skills.contains(skill);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return id.equals(vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
