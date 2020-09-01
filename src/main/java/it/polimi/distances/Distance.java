package it.polimi.distances;

import it.polimi.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Distance {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Distance.class);

    private Map<Location, Integer> indicesMap;

    public Distance(final List<Location> locations) {
        buildMatrices(locations);
        this.indicesMap = new HashMap<>();
        for (int i=0; i<locations.size(); i++)
            this.indicesMap.put(locations.get(i), i);
    }

    protected abstract void buildMatrices(List<Location> locations);
    public abstract float[][] getDistancesMatrix();
    public abstract float[][] getDurationsMatrix();

    public float distance(Location l1, Location l2) {
        float[][] distMatrix = getDistancesMatrix();
        return distMatrix[indicesMap.get(l1)][indicesMap.get(l2)];
    }

    public float duration(Location l1, Location l2) {
        float[][] durMatrix = getDurationsMatrix();
        return durMatrix[indicesMap.get(l1)][indicesMap.get(l2)];
    }
}
