package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

public interface StoppingCondition {
    boolean isStopping(Solution s);
}
