package it.polimi.domain;

import java.util.Objects;

public class Customer {
    private Location location;
    private int releaseDate;
    private int days;
    private int score;

    public Customer(final Location location, final int releaseDate, final int days, final int score) {
        this.location = location;
        this.releaseDate = releaseDate;
        this.days = days;
        this.score = score;
    }

    public Location getLocation() {
        return location;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public int getDays() {
        return days;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        final Customer customer = (Customer) o;
        return getDays() == customer.getDays() &&
                getScore() == customer.getScore() &&
                Objects.equals(getLocation(), customer.getLocation()) &&
                Objects.equals(getReleaseDate(), customer.getReleaseDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation(), getReleaseDate(), getDays(), getScore());
    }
}
