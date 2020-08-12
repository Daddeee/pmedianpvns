package it.polimi.distances;

import it.polimi.domain.Location;

import java.util.List;

public class Haversine extends Distance {
    private static final int EARTH_RADIUS = 6371000; // meters

    public float[][] distanceMatrix;
    public float[][] durationsMatrix;

    public Haversine(final List<Location> locations) {
        super(locations);
    }

    @Override
    public float[][] getDistancesMatrix() {
        return distanceMatrix;
    }

    @Override
    public float[][] getDurationsMatrix() {
        return durationsMatrix;
    }

    @Override
    protected void buildMatrices(final List<Location> locations) {
        this.distanceMatrix = new float[locations.size()][locations.size()];
        this.durationsMatrix = new float[locations.size()][locations.size()];

        for (int i=0; i < locations.size()-1; i++) {
            for (int j=i+1; j < locations.size(); j++) {
                Location l1 = locations.get(i), l2 = locations.get(j);
                float d = distance(l1.getLat(), l1.getLng(), l2.getLat(), l2.getLng());
                distanceMatrix[i][j] = d;
                distanceMatrix[j][i] = d;
                durationsMatrix[i][j] = d;
                durationsMatrix[j][i] = d;
            }
        }
    }

    public static float distance(double startLat, double startLong,
                                 double endLat, double endLong) {

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * ((float) c); // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
