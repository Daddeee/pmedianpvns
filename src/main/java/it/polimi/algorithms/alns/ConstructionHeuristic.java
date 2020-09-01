package it.polimi.algorithms.alns;

import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

public interface ConstructionHeuristic {
    Solution build(Problem p);
}
