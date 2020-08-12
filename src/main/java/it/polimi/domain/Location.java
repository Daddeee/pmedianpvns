package it.polimi.domain;

import java.util.Objects;

public class Location {
    private String id;
    private double lat;
    private double lng;

    public Location(final String id, final double lat, final double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        final Location location = (Location) o;
        return getId().equals(location.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
