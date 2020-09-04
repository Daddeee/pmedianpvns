package it.polimi.algorithms.mpbpmp.domain;

import it.polimi.domain.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.text.resources.ga.JavaTimeSupplementary_ga;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MPBPMPSolution {
    private static final Logger LOGGER = LoggerFactory.getLogger(MPBPMPSolution.class);
    private int[] periods;
    private int[] medians;
    private double objective;
    private double elapsedTime;
    private Map<Integer, List<Integer>> pointsPerPeriod;
    private Map<Integer, List<Integer>> pointsPerMedian;

    public MPBPMPSolution(int[] periods, int[] medians, double objective, double elapsedTime) {
        if (periods.length != medians.length)
            throw new IllegalArgumentException("Wrong sizes for periods and medians: (" + periods.length + "," + medians.length + ")");
        this.periods = periods;
        this.medians = medians;
        this.objective = objective;
        this.elapsedTime = elapsedTime;
        this.pointsPerPeriod = new HashMap<>();
        this.pointsPerMedian = new HashMap<>();
        for (int i=0; i<periods.length; i++) {
            addToMapOfLists(periods[i], i, pointsPerPeriod);
            addToMapOfLists(medians[i], i, pointsPerMedian);
        }
    }

    public MPBPMPSolution(int[] periods, int[] medians, double objective, double elapsedTime, Map<Integer,
            List<Integer>> pointsPerPeriod, Map<Integer, List<Integer>> pointsPerMedian) {
        this.periods = periods;
        this.medians = medians;
        this.objective = objective;
        this.elapsedTime = elapsedTime;
        this.pointsPerPeriod = pointsPerPeriod;
        this.pointsPerMedian = pointsPerMedian;
    }

    public int[] getPeriods() {
        return periods;
    }

    public int[] getMedians() {
        return medians;
    }

    public double getObjective() {
        return objective;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public Map<Integer, List<Integer>> getPointsPerPeriod() {
        return pointsPerPeriod;
    }

    public Map<Integer, List<Integer>> getPointsPerMedian() {
        return pointsPerMedian;
    }

    public void setObjective(double objective) {
        this.objective = objective;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public MPBPMPSolution clone() {
        return new MPBPMPSolution(periods.clone(), medians.clone(), objective, elapsedTime,
                cloneMapOfLists(pointsPerPeriod), cloneMapOfLists(pointsPerMedian));
    }

    public void setPeriod(int point, int newPeriod, MPBPMProblem problem) {
        double removalDelta = problem.getC()[point][medians[point]];
        removalDelta += (pointsPerMedian.get(medians[point]).size() <= problem.getAvg()) ? -problem.getAlpha() : problem.getAlpha();
        double minDelta = Double.MAX_VALUE;
        int newMedian = -1;
        for (int i : pointsPerPeriod.get(newPeriod)) {
            if (i == medians[i]) { // if i is a median in period newPeriod
                double posDelta = problem.getC()[point][i];
                posDelta += (pointsPerMedian.get(i).size() < problem.getAvg()) ? -problem.getAlpha() : problem.getAlpha();
                if (posDelta < minDelta) {
                    minDelta = posDelta;
                    newMedian = i;
                }
            }
        }
        pointsPerPeriod.get(periods[point]).remove(new Integer(point));
        addToMapOfLists(newPeriod, point, pointsPerPeriod);
        periods[point] = newPeriod;
        pointsPerMedian.get(medians[point]).remove(new Integer(point));
        addToMapOfLists(newMedian, point, pointsPerMedian);
        medians[point] = newMedian;
        objective += minDelta;
        objective -= removalDelta;
    }

    public void setMedian(int point, int newMedian, MPBPMProblem problem) {
        if (periods[point] != periods[newMedian])
            throw new IllegalArgumentException("Swapping medians across periods. point=" + point + ". period="
                    + periods[point] + ", newMedian=" + newMedian + ", newPeriod=" + periods[newMedian]);
        int oldMedian = medians[point];
        if (newMedian == oldMedian)
            return;
        LOGGER.info("Swapping median of " + point + " from " + oldMedian + " to " + newMedian);
        int oldMedianSize = pointsPerMedian.get(oldMedian).size();
        int newMedianSize = pointsPerMedian.getOrDefault(newMedian, new ArrayList<>()).size() + 1;
        double distDelta =  - problem.getC()[point][oldMedian] + problem.getC()[point][newMedian];
        double balanceDelta = - problem.getAlpha() * Math.abs(oldMedianSize - problem.getAvg())
                + problem.getAlpha() * Math.abs(newMedianSize - problem.getAvg());

        pointsPerMedian.get(oldMedian).remove(new Integer(point));
        if (pointsPerMedian.get(oldMedian).size() == 0)
            pointsPerMedian.remove(oldMedian);
        addToMapOfLists(newMedian, point, pointsPerMedian);
        medians[point] = newMedian;
        LOGGER.info("Computed delta. tot=" + (distDelta + balanceDelta) + ", dist=" + distDelta + ", balance="
                + balanceDelta + ", old=" + objective);
        objective += distDelta + balanceDelta;
    }

    public void addToMedian(int point, int median) {
        addToMapOfLists(median, point, pointsPerMedian);
    }

    public void addToPeriod(int point, int period) {
        addToMapOfLists(period, point, pointsPerPeriod);
    }

    private Map<Integer, List<Integer>> cloneMapOfLists(Map<Integer, List<Integer>> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
    }

    private void addToMapOfLists(Integer key, Integer value, Map<Integer, List<Integer>> map) {
        List<Integer> list = map.getOrDefault(key, new ArrayList<>());
        list.add(value);
        map.put(key, list);
    }
}
