package it.polimi.distances;

import it.polimi.domain.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Euclidean extends Distance {

    private float[][] distanceMatrix;
    private float[][] durationsMatrix;
    private Map<String, Integer> indicesMap;

    public Euclidean(List<Location> locations) {
        super(locations);
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

    @Override
    public float[][] getDistancesMatrix() {
        return distanceMatrix;
    }

    @Override
    public float[][] getDurationsMatrix() {
        return durationsMatrix;
    }

    public static float distance(final double startLat, final double startLng,
                                 final double endLat, final double endLng) {
        return (float) Math.sqrt(Math.pow(endLat - startLat, 2) + Math.pow(endLng - startLng, 2));
    }
}
