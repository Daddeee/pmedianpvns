package it.polimi.algorithms.pmp.objectives;

import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.algorithms.pmp.domain.PMPSolution;

import java.util.*;

public class BalancedTotalDistance extends PMPObjectiveFunction {
    private final Distance dist;
    private final int numMedians;
    private final double avg;
    private final double balancePenalty;

    public BalancedTotalDistance(double unassignedMedianPenalty, Distance dist, int numMedians, double avg,
                                 double balancePenalty) {
        super(unassignedMedianPenalty);
        this.dist = dist;
        this.numMedians = numMedians;
        this.avg = avg;
        this.balancePenalty = balancePenalty;
    }

    public double getAvg() {
        return avg;
    }

    public double getBalancePenalty() {
        return balancePenalty;
    }

    @Override
    public double getCost(PMPSolution solution) {
        double cost = 0;

        // total distance between location and medians
        Map<Location, Integer> counts = new HashMap<>();
        for (Location location : solution.getLocationMedians().keySet()) {
            Location median = solution.getLocationMedians().get(location);
            cost += dist.distance(location, median);
            int count = counts.getOrDefault(median, 0);
            counts.put(median, count + 1);
        }

        // avg difference between number of locations per medians and avg
        for (int count : counts.values())
            cost += balancePenalty*Math.abs(count - avg);

        // missing medians penalty
        cost += Math.abs(numMedians - solution.getMedians().size()) * unassignedMedianPenalty;

        return cost;
    }

    @Override
    public double getInsertionDelta(Location toInsert, PMPSolution solution) {
        double delta = 0.;

        // inserting a new median so reducing the unassigned penalty (assuming never inserting over p)
        delta -= unassignedMedianPenalty;

        // change in distances and counts for new medians
        Map<Location, Integer> oldCounts = new HashMap<>(), newCounts = new HashMap<>();
        int toInsertCount = 0;
        for (Location location : solution.getLocationMedians().keySet()) {
            Location median = solution.getLocationMedians().get(location);
            oldCounts.put(median, oldCounts.getOrDefault(median, 0) + 1);
            double distFromMedian = dist.distance(location, median);
            double distFromInsert = dist.distance(location, toInsert);
            if (distFromInsert < distFromMedian) {
                delta -= distFromMedian;
                delta += distFromInsert;
                toInsertCount++;
            } else {
                newCounts.put(median, newCounts.getOrDefault(median, 0) + 1);
            }
        }

        // updating count penalties
        for (Location location : oldCounts.keySet()) {
            int oldc = oldCounts.get(location);
            int newc = newCounts.getOrDefault(location, 0);
            delta -= balancePenalty * Math.abs(oldc - avg);
            delta += balancePenalty * Math.abs(newc - avg);
        }
        delta += balancePenalty * toInsertCount;


        return delta;
    }

    @Override
    public double getRemovalDelta(List<Location> toRemove, PMPSolution solution) {
        double delta = 0.;

        // removing so increasing the penalty due to unassigned (assuming never having more than p medians)
        delta += toRemove.size() * unassignedMedianPenalty;

        // updating counts and distances
        Set<Location> toRemoveSet = new HashSet<>(toRemove);
        Map<Location, Integer> oldCounts = new HashMap<>(), newCounts = new HashMap<>();
        for (Location location : solution.getLocationMedians().keySet()) {
            Location median = solution.getLocationMedians().get(location);
            oldCounts.put(median, oldCounts.getOrDefault(median, 0) + 1);
            if (toRemoveSet.contains(median)) {
                Location backupMedian = solution.getLocationBakcupMedians().get(location);
                delta -= dist.distance(location, median);
                delta += dist.distance(location, backupMedian);
                newCounts.put(backupMedian, newCounts.getOrDefault(backupMedian, 0) + 1);
            } else {
                newCounts.put(median, newCounts.getOrDefault(median, 0) + 1);
            }
        }

        // updating count penalties
        for (Location location : newCounts.keySet()) {
            int newc = newCounts.get(location);
            int oldc = oldCounts.getOrDefault(location, 0);
            delta -= balancePenalty * Math.abs(oldc - avg);
            delta += balancePenalty * Math.abs(newc - avg);
        }

        return delta;
    }
}
