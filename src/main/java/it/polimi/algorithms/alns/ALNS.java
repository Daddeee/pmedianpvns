package it.polimi.algorithms.alns;

import it.polimi.algorithms.vrp.recreate.GreedyInsertion;
import it.polimi.algorithms.vrp.ruin.WorstRemoval;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ALNS {
    private static final Logger LOGGER = LoggerFactory.getLogger(ALNS.class);
    private final int segmentSize;
    private final StoppingCondition stoppingCondition;
    private final OperatorsSelector operatorsSelector;
    private final AcceptanceCriterion acceptanceCriterion;
    private final List<RecreateOperator> recreateOperators;
    private final List<RuinOperator> ruinOperators;
    private final HashSet<Solution> accepted;

    public ALNS(final int segmentSize, final StoppingCondition stoppingCondition,
                final OperatorsSelector operatorsSelector, final AcceptanceCriterion acceptanceCriterion) {
        this.segmentSize = segmentSize;
        this.stoppingCondition = stoppingCondition;
        this.operatorsSelector = operatorsSelector;
        this.acceptanceCriterion = acceptanceCriterion;
        this.ruinOperators = new ArrayList<>();
        this.recreateOperators = new ArrayList<>();
        this.accepted = new HashSet<>();
    }

    public void addRuinOperator(final RuinOperator ruinOperator) {
        this.ruinOperators.add(ruinOperator);
    }

    public void addRecreateOperator(final RecreateOperator recreateOperator) {
        this.recreateOperators.add(recreateOperator);
    }

    public Solution run(final Problem problem, final Solution initialSolution) {
        operatorsSelector.setOperatorSizes(ruinOperators.size(), recreateOperators.size());
        Solution s = initialSolution;
        Solution smin = s;
        int iterations = 0;
        int segmentCount = 0;
        while(!stoppingCondition.isStopping(s)) {
            segmentCount++;
            LOGGER.info("Starting segment " + segmentCount);
            for (int i=0; i<segmentSize; i++) {
                iterations++;
                int ruinIdx = operatorsSelector.selectRuinOperatorIndex();
                int recreateIdx = operatorsSelector.selectRecreateOperatorIndex();
                RuinOperator d = ruinOperators.get(ruinIdx);
                RecreateOperator r = recreateOperators.get(recreateIdx);

                Solution s1 = r.recreate(d.ruin(s));

                if (acceptanceCriterion.accept(s1, s)) {
                    LOGGER.info("Accepted solution at iteration " + iterations + ", obj=" + s1.getCost());
                    if (!accepted.contains(s1))
                        operatorsSelector.update(ruinIdx, recreateIdx, s1, s, smin);
                    accepted.add(s1);
                    s = s1;
                    if (s.getCost() < smin.getCost()) {
                        LOGGER.info("Best solution so far.");
                        smin = s;
                    }
                }
            }
            operatorsSelector.updateWeights();
        }
        return smin;
    }
}
