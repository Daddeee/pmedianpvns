package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

import java.util.List;

public class MultipleStopping implements StoppingCondition {
    private List<StoppingCondition> stoppingConditions;

    public MultipleStopping(List<StoppingCondition> stoppingConditions) {
        this.stoppingConditions = stoppingConditions;
    }

    @Override
    public boolean isStopping(Solution s) {
        return stoppingConditions.stream().anyMatch(st -> st.isStopping(s));
    }
}
