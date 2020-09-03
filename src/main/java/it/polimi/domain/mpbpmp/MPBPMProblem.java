package it.polimi.domain.mpbpmp;

import it.polimi.distances.Distance;
import it.polimi.domain.Problem;
import it.polimi.domain.Customer;

import java.util.List;

public class MPBPMProblem implements Problem {
    private List<Service> services;
    private int numPeriods;
    private int numMedians;
    private Distance distance;

    public MPBPMProblem(List<Service> services, int numPeriods, int numMedians, Distance distance) {
        this.services = services;
        this.numPeriods = numPeriods;
        this.numMedians = numMedians;
        this.distance = distance;
    }

    public List<Service> getServices() {
        return services;
    }

    public int getNumPeriods() {
        return numPeriods;
    }

    public int getNumMedians() {
        return numMedians;
    }

    public Distance getDistance() {
        return distance;
    }
}
