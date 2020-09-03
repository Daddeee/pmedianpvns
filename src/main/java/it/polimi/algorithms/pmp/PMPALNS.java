package it.polimi.algorithms.pmp;

import it.polimi.algorithms.alns.ALNS;
import it.polimi.algorithms.alns.AcceptanceCriterion;
import it.polimi.algorithms.alns.OperatorsSelector;
import it.polimi.algorithms.alns.StoppingCondition;
import it.polimi.algorithms.pmp.recreate.GreedyInsertion;
import it.polimi.algorithms.pmp.recreate.GreedyInsertionWithNoise;
import it.polimi.algorithms.pmp.ruin.RandomRemoval;
import it.polimi.algorithms.pmp.ruin.WorstRemoval;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.algorithms.pmp.domain.PMProblem;

public class PMPALNS extends ALNS {
    private double maxN;
    private Integer seed;

    public PMPALNS(double maxN, int segmentSize, StoppingCondition stoppingCondition, OperatorsSelector operatorsSelector,
                   AcceptanceCriterion acceptanceCriterion, int seed) {
        super(segmentSize, stoppingCondition, operatorsSelector, acceptanceCriterion);
        this.maxN = maxN;
        this.seed = seed;
    }

    public PMPALNS(double maxN, int segmentSize, StoppingCondition stoppingCondition, OperatorsSelector operatorsSelector,
                   AcceptanceCriterion acceptanceCriterion) {
        super(segmentSize, stoppingCondition, operatorsSelector, acceptanceCriterion);
        this.maxN = maxN;
        this.seed = null;
    }

    @Override
    public Solution run(Problem problem, Solution initialSolution) {
        PMProblem pmp = (PMProblem) problem;
        if (seed != null) {
            this.addRuinOperator(new RandomRemoval(pmp, 0.2, seed));
            this.addRuinOperator(new WorstRemoval(pmp, seed));
            this.addRecreateOperator(new GreedyInsertionWithNoise(pmp, maxN));
            this.addRecreateOperator(new GreedyInsertion(pmp));
        } else {
            this.addRuinOperator(new RandomRemoval(pmp, 0.2));
            this.addRuinOperator(new WorstRemoval(pmp));
            this.addRecreateOperator(new GreedyInsertionWithNoise(pmp, maxN));
            this.addRecreateOperator(new GreedyInsertion(pmp));
        }
        return super.run(problem, initialSolution);
    }
}
