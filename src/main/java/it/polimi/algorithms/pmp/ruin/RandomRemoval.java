package it.polimi.algorithms.pmp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.algorithms.pmp.domain.PMProblem;
import it.polimi.algorithms.pmp.domain.PMPSolution;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RandomRemoval implements RuinOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomRemoval.class);
    private final PMProblem pmp;
    private final double eps;
    private final Random random;

    public RandomRemoval(PMProblem pmp, double eps) {
        this.pmp = pmp;
        this.eps = eps;
        this.random = new Random();
    }

    public RandomRemoval(PMProblem pmp, double eps, int seed) {
        this.pmp = pmp;
        this.eps = eps;
        this.random = new Random(seed);
    }

    @Override
    public Solution ruin(Solution s) {
        PMPSolution pmps = ((PMPSolution) s).clone();
        pmps.getRemoved().clear();
        int q = 2;//Math.max(2, random.nextInt(pmps.getMedians().size() - 1));
        for (int i=0; i<q; i++) {
            Location toRemove = Rand.sampleAndRemove(pmps.getMedians(), random);
            pmps.getRemoved().add(toRemove);
        }
        for (Location location : pmp.getLocations()) {
            Location median = pmps.getLocationMedians().get(location);
            Location backupMedian = pmps.getLocationBakcupMedians().get(location);
            if (pmps.getRemoved().contains(median)) {
                pmps.getLocationMedians().put(location, backupMedian);
                pmps.getLocationBakcupMedians().put(location, findBackupMedian(location, pmps.getMedians(), backupMedian));
            }
        }
        pmps.setCost(pmp.getObjectiveFunction().getCost(pmps));
        return pmps;
    }

    private Location findBackupMedian(Location location, List<Location> candidates, Location actualMedian) {
        double minD = Double.MAX_VALUE;
        Location bakcupMedian = null;
        for (Location candidate : candidates) {
            if (candidate.equals(actualMedian)) continue;
            double d = pmp.getDistance().distance(location, candidate);
            if (d < minD) {
                minD = d;
                bakcupMedian = candidate;
            }
        }
        return bakcupMedian;
    }
}
