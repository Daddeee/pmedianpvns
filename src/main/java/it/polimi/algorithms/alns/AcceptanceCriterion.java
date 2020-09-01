package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

public interface AcceptanceCriterion {
    boolean accept(Solution incoming, Solution current);
}
