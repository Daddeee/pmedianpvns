package it.polimi.domain;

import java.util.Calendar;
import java.util.Objects;

public class Service {
    private Location location;
    private Calendar releaseDate;
    private int days;
    private int score;

    public Service(final Location location, final Calendar releaseDate, final int days, final int score) {
        this.location = location;
        this.releaseDate = releaseDate;
        this.days = days;
        this.score = score;
    }

    public Location getLocation() {
        return location;
    }

    public Calendar getReleaseDate() {
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
        if (!(o instanceof Service)) return false;
        final Service service = (Service) o;
        return getDays() == service.getDays() &&
                getScore() == service.getScore() &&
                Objects.equals(getLocation(), service.getLocation()) &&
                Objects.equals(getReleaseDate(), service.getReleaseDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation(), getReleaseDate(), getDays(), getScore());
    }
}
