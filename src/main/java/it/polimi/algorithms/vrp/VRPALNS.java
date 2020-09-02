package it.polimi.algorithms.vrp;

import it.polimi.algorithms.alns.*;
import it.polimi.algorithms.vrp.recreate.GreedyInsertion;
import it.polimi.algorithms.vrp.recreate.GreedyInsertionWithNoise;
import it.polimi.algorithms.vrp.recreate.RegretInsertion;
import it.polimi.algorithms.vrp.recreate.RegretInsertionWithNoise;
import it.polimi.algorithms.vrp.ruin.RandomRemoval;
import it.polimi.algorithms.vrp.ruin.ShawRemoval;
import it.polimi.algorithms.vrp.ruin.WorstRemoval;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.domain.vrp.VehicleRoutingProblem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VRPALNS extends ALNS {

    public static class Builder {
        private double eps = 0.4;
        private int p = 6;
        private int pworst = 3;
        private double phi = 9;
        private double chi = 3;
        private double psi = 2;
        private int segmentSize = 100;
        private int maxNumSegments = 50;
        private double d1 = 33;
        private double d2 = 9;
        private double d3 = 13;
        private double r = 0.1;
        private double tstart = 10000;
        private double c = 0.99;
        private double maxN = 0.025*1000;
        private Integer seed = null;

        public Builder() {
        }

        public Builder setEps(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder setP(int p) {
            this.p = p;
            return this;
        }

        public Builder setPworst(int pworst) {
            this.pworst = pworst;
            return this;
        }

        public Builder setPhi(double phi) {
            this.phi = phi;
            return this;
        }

        public Builder setChi(double chi) {
            this.chi = chi;
            return this;
        }

        public Builder setPsi(double psi) {
            this.psi = psi;
            return this;
        }

        public Builder setMaxN(double maxN) {
            this.maxN = maxN;
            return this;
        }

        public Builder setSegmentSize(int segmentSize) {
            this.segmentSize = segmentSize;
            return this;
        }

        public Builder setMaxNumSegments(int maxNumSegments) {
            this.maxNumSegments = maxNumSegments;
            return this;
        }

        public Builder setD1(double d1) {
            this.d1 = d1;
            return this;
        }

        public Builder setD2(double d2) {
            this.d2 = d2;
            return this;
        }

        public Builder setD3(double d3) {
            this.d3 = d3;
            return this;
        }

        public Builder setR(double r) {
            this.r = r;
            return this;
        }

        public Builder setTstart(double tstart) {
            this.tstart = tstart;
            return this;
        }

        public Builder setC(double c) {
            this.c = c;
            return this;
        }

        public Builder setSeed(Integer seed) {
            this.seed = seed;
            return this;
        }

        public VRPALNS build() {
            StoppingCondition stoppingCondition = new MaxIterationStopping(this.maxNumSegments);
            if (seed != null) {
                OperatorsSelector operatorsSelector = new OperatorsSelector(this.d1, this.d2, this.d3, this.r, seed);
                AcceptanceCriterion acceptanceCriterion = new TemperatureAcceptance(this.tstart, this.c, seed);
                return new VRPALNS(eps, p, pworst, phi, chi, psi, maxN, segmentSize, stoppingCondition, operatorsSelector,
                        acceptanceCriterion, seed);
            }
            else {
                OperatorsSelector operatorsSelector = new OperatorsSelector(this.d1, this.d2, this.d3, this.r);
                AcceptanceCriterion acceptanceCriterion = new TemperatureAcceptance(this.tstart, this.c);
                return new VRPALNS(eps, p, pworst, phi, chi, psi, maxN, segmentSize, stoppingCondition, operatorsSelector,
                        acceptanceCriterion);
            }
        }
    }

    private final double eps;
    private final int p;
    private final int pworst;
    private final double phi;
    private final double chi;
    private final double psi;
    private final double maxN;
    private final Integer seed;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public VRPALNS(double eps, int p, int pworst, double phi, double chi, double psi, double maxN, int segmentSize,
                   StoppingCondition stoppingCondition, OperatorsSelector operatorsSelector,
                   AcceptanceCriterion acceptanceCriterion, int seed) {
        super(segmentSize, stoppingCondition, operatorsSelector, acceptanceCriterion);
        this.eps = eps;
        this.p = p;
        this.pworst = pworst;
        this.phi = phi;
        this.chi = chi;
        this.psi = psi;
        this.maxN = maxN;
        this.seed = seed;
    }

    public VRPALNS(double eps, int p, int pworst, double phi, double chi, double psi, double maxN, int segmentSize,
                   StoppingCondition stoppingCondition, OperatorsSelector operatorsSelector,
                   AcceptanceCriterion acceptanceCriterion) {
        super(segmentSize, stoppingCondition, operatorsSelector, acceptanceCriterion);
        this.eps = eps;
        this.p = p;
        this.pworst = pworst;
        this.phi = phi;
        this.chi = chi;
        this.psi = psi;
        this.maxN = maxN;
        this.seed = null;
    }

    @Override
    public Solution run(Problem problem, Solution initialSolution) {
        VehicleRoutingProblem vrp = (VehicleRoutingProblem) problem;
        if (seed != null) {
            this.addRuinOperator(new RandomRemoval(vrp, eps, seed));
            this.addRuinOperator(new ShawRemoval(vrp, p, eps, phi, chi, psi, seed));
            this.addRuinOperator(new WorstRemoval(vrp, pworst, eps, seed));
            this.addRecreateOperator(new GreedyInsertion(vrp, executorService));
            this.addRecreateOperator(new RegretInsertion(vrp, executorService));
            this.addRecreateOperator(new GreedyInsertionWithNoise(vrp, executorService, maxN, seed));
            this.addRecreateOperator(new RegretInsertionWithNoise(vrp, executorService, maxN, seed));
        } else {
            this.addRuinOperator(new RandomRemoval(vrp, eps));
            this.addRuinOperator(new ShawRemoval(vrp, p, eps, phi, chi, psi));
            this.addRuinOperator(new WorstRemoval(vrp, pworst, eps));
            this.addRecreateOperator(new GreedyInsertion(vrp, executorService));
            this.addRecreateOperator(new RegretInsertion(vrp, executorService));
            this.addRecreateOperator(new GreedyInsertionWithNoise(vrp, executorService, maxN));
            this.addRecreateOperator(new RegretInsertionWithNoise(vrp, executorService, maxN));
        }
        return super.run(problem, initialSolution);
    }

    public static Builder getBuilder() {
        return new Builder();
    }
}
