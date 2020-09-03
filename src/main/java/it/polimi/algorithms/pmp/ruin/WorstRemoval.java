package it.polimi.algorithms.pmp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.algorithms.pmp.objectives.BalancedTotalDistance;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.algorithms.pmp.domain.PMPSolution;
import it.polimi.algorithms.pmp.domain.PMProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WorstRemoval implements RuinOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorstRemoval.class);
    private final PMProblem pmp;
    private final Random random;

    public WorstRemoval(PMProblem pmp) {
        this.pmp = pmp;
        this.random = new Random();
    }

    public WorstRemoval(PMProblem pmp, int seed) {
        this.pmp = pmp;
        this.random = new Random(seed);
    }

    @Override
    public Solution ruin(Solution s) {
        PMPSolution pmps = ((PMPSolution) s).clone();
        pmps.getRemoved().clear();
        int q = Math.max(2, random.nextInt(pmps.getMedians().size() - 1));
        for (int i=0; i<q; i++) {
            Location toRemove = getWorst(pmps);
            remove(toRemove, pmps);
        }
        return pmps;
    }

    private Location getWorst(PMPSolution solution) {
        Map<Location, Double> costs = new HashMap<>();
        Map<Location, Integer> counts = new HashMap<>();
        for (Location location : pmp.getLocations()) {
            Location median = solution.getLocationMedians().get(location);
            costs.put(median, costs.getOrDefault(median, 0.) + pmp.getDistance().distance(location, median));
            counts.put(median, counts.getOrDefault(median, 0) + 1);
        }
        double maxCost = Double.MIN_VALUE;
        Location worst = null;
        double alpha = ((BalancedTotalDistance) pmp.getObjectiveFunction()).getBalancePenalty();
        double avg = ((BalancedTotalDistance) pmp.getObjectiveFunction()).getAvg();
        for (Location median : costs.keySet()) {
            double cost = costs.get(median) + alpha * Math.abs(counts.get(median) - avg);
            if (cost > maxCost) {
                maxCost = cost;
                worst = median;
            }
        }
        return worst;
    }

    private void remove(Location toRemove, PMPSolution solution) {
        solution.getMedians().remove(toRemove);
        solution.getRemoved().add(toRemove);
        for (Location location : pmp.getLocations()) {
            Location median = solution.getLocationMedians().get(location);
            Location backupMedian = solution.getLocationBakcupMedians().get(location);
            if (toRemove.equals(median)) {
                solution.getLocationMedians().put(location, backupMedian);
                solution.getLocationBakcupMedians().put(location, findBackupMedian(location, solution.getMedians(), backupMedian));
            }
        }
        solution.setCost(pmp.getObjectiveFunction().getCost(solution));
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
