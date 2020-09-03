package it.polimi.algorithms.pmp.recreate;

import it.polimi.algorithms.alns.RecreateOperator;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.algorithms.pmp.domain.PMPSolution;
import it.polimi.algorithms.pmp.domain.PMProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GreedyInsertion implements RecreateOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreedyInsertion.class);
    private PMProblem pmp;

    public GreedyInsertion(PMProblem pmp) {
        this.pmp = pmp;
    }

    @Override
    public Solution recreate(Solution s) {
        PMPSolution pmps = ((PMPSolution) s).clone();
        Set<Location> medians = new HashSet<>(pmps.getMedians());
        Set<Location> forbidden = new HashSet<>(pmps.getRemoved());
        while (medians.size() < pmp.getNumMedians()) {
            double bestDelta = Double.MAX_VALUE;
            Location bestLocation = null;
            for (Location location : pmp.getLocations()) {
                if (medians.contains(location) || forbidden.contains(location)) continue;
                double delta = getInsertionDelta(pmps, location);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestLocation = location;
                }
            }
            for (Location location : pmp.getLocations()) {
                Location median = pmps.getLocationMedians().get(location);
                Location backupMedian = pmps.getLocationBakcupMedians().get(location);
                double insertDist = pmp.getDistance().distance(location, bestLocation);
                double medianDist = pmp.getDistance().distance(location, median);
                double backupDist = pmp.getDistance().distance(location, backupMedian);
                if (insertDist < medianDist) {
                    pmps.getLocationMedians().put(location, bestLocation);
                    pmps.getLocationBakcupMedians().put(location, backupMedian);
                } else if (insertDist < backupDist) {
                    pmps.getLocationBakcupMedians().put(location, bestLocation);
                }
            }
            pmps.getMedians().add(bestLocation);
            pmps.setCost(pmp.getObjectiveFunction().getCost(pmps));
            medians.add(bestLocation);
        }
        return pmps;
    }

    protected double getInsertionDelta(PMPSolution pmps, Location location) {
        return pmp.getObjectiveFunction().getInsertionDelta(location, pmps);
    }
}
