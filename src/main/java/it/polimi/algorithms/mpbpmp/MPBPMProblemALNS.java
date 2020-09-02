package it.polimi.algorithms.mpbpmp;

import it.polimi.algorithms.alns.ALNS;
import it.polimi.algorithms.alns.AcceptanceCriterion;
import it.polimi.algorithms.alns.OperatorsSelector;
import it.polimi.algorithms.alns.StoppingCondition;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

public class MPBPMProblemALNS extends ALNS {

    public MPBPMProblemALNS(int segmentSize, StoppingCondition stoppingCondition, OperatorsSelector operatorsSelector,
                            AcceptanceCriterion acceptanceCriterion) {
        super(segmentSize, stoppingCondition, operatorsSelector, acceptanceCriterion);

    }

    @Override
    public Solution run(Problem problem, Solution initialSolution) {
        return super.run(problem, initialSolution);
    }
}
