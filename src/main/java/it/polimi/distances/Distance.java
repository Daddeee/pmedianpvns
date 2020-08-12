package it.polimi.distances;

import it.polimi.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class Distance {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Distance.class);

    protected Distance(final List<Location> locations) {
        LOGGER.info("Start distances calculation.");
        long start = System.nanoTime();
        buildMatrices(locations);
        long end = System.nanoTime();
        double elapsedTime = (end - start) / 1e6;
        LOGGER.info("Distances calculation completed. Elapsed time: " + elapsedTime + " ms");
    }

    protected abstract void buildMatrices(List<Location> locations);
    public abstract float[][] getDistancesMatrix();
    public abstract float[][] getDurationsMatrix();
}
