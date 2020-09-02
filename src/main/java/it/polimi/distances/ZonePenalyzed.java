package it.polimi.distances;

import it.polimi.domain.Location;

import java.util.List;

public class ZonePenalyzed extends Distance {

    private final List<Location> locations;
    private final Distance original;
    private final double penaltyRatio;
    private final List<Integer> locationsZones;

    private float[][] distanceMatrix;
    private float[][] durationsMatrix;

    public ZonePenalyzed(List<Location> locations, Distance original, double penaltyRatio, List<Integer> locationsZones) {
        super(locations);
        this.locations = locations;
        this.original = original;
        this.penaltyRatio = penaltyRatio;
        this.locationsZones = locationsZones;
    }

    @Override
    public void buildMatrices(List<Location> locations) {
    }

    public void applyPenalty() {
        this.distanceMatrix = original.getDistancesMatrix();
        this.durationsMatrix = original.getDurationsMatrix();
        for (int i = 0; i< locations.size(); i++) {
            for (int j=0; j<locations.size(); j++) {
                int iZone = locationsZones.get(i), jZone = locationsZones.get(j);
                if (iZone == -1 || jZone == -1 || iZone == jZone) continue;
                distanceMatrix[i][j] += penaltyRatio * distanceMatrix[i][j];
                durationsMatrix[i][j] += penaltyRatio * durationsMatrix[i][j];
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
}
