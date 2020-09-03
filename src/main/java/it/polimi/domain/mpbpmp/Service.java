package it.polimi.domain.mpbpmp;

import it.polimi.domain.Location;

import java.util.Objects;

public class Service {
    private String id;
    private Location location;
    private int releaseDate;
    private int dueDate;

    public Service(String id, Location location, int releaseDate, int dueDate) {
        this.id = id;
        this.location = location;
        this.releaseDate = releaseDate;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public int getDueDate() {
        return dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(id, service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
