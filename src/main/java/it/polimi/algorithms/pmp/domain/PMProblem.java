package it.polimi.algorithms.pmp.domain;

import it.polimi.algorithms.pmp.objectives.PMPObjectiveFunction;
import it.polimi.distances.Distance;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;

import java.util.List;

public class PMProblem implements Problem {
    private List<Location> locations;
    private Distance distance;
    private int numMedians;
    private PMPObjectiveFunction objectiveFunction;

    public PMProblem(List<Location> locations, Distance distance, int numMedians, PMPObjectiveFunction objectiveFunction) {
        this.locations = locations;
        this.distance = distance;
        this.numMedians = numMedians;
        this.objectiveFunction = objectiveFunction;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public Distance getDistance() {
        return distance;
    }

    public int getNumMedians() {
        return numMedians;
    }

    public PMPObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }
}
