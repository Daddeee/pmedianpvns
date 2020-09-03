package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

import java.util.Random;

public class TemperatureAcceptance implements AcceptanceCriterion {

    private double temperature;
    private final double cooling;
    private final Random random;

    public TemperatureAcceptance(double temperature, double cooling) {
        this(temperature, cooling, new Random());
    }

    public TemperatureAcceptance(double temperature, double cooling, int seed) {
        this(temperature, cooling, new Random(seed));
    }

    public TemperatureAcceptance(double temperature, double cooling, Random random) {
        this.temperature = temperature;
        this.cooling = cooling;
        this.random = random;
    }

    @Override
    public boolean accept(Solution incoming, Solution current) {
        double incomingCost = incoming.getCost();
        double currentCost = current.getCost();
        double prob = Math.exp(-(incomingCost - currentCost)/this.temperature);
        this.temperature *= this.cooling;
        return random.nextDouble() < prob;
    }
}
