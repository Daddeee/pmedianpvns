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

    public MPBPMPSolution(int[] periods, int[] medians, double objective, double elapsedTime) {
        if (periods.length != medians.length)
            throw new IllegalArgumentException("Wrong sizes for periods and medians: (" + periods.length + "," + medians.length + ")");
        this.periods = periods;
        this.medians = medians;
        this.objective = objective;
        this.elapsedTime = elapsedTime;
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

    public void setObjective(double objective) {
        this.objective = objective;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public MPBPMPSolution clone() {
        return new MPBPMPSolution(periods.clone(), medians.clone(), objective, elapsedTime);
    }

    public int getPeriod(int point) {
        return periods[point];
    }

    public void setPeriod(int point, int newPeriod) {
        periods[point] = newPeriod;
    }

    public int getMedian(int point) {
        return medians[point];
    }

    public void setMedian(int point, int newMedian) {
        medians[point] = newMedian;
    }

    public List<Integer> getPointsInPeriod(int period) {
        return IntStream.range(0, periods.length).filter(i -> periods[i] == period).boxed().collect(Collectors.toList());
    }
}
