package it.polimi.algorithms.mpbpmp;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Customer;
import it.polimi.io.TestCSVReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MPBPM {
    public static void main(String[] args) {
        TestCSVReader reader = new TestCSVReader();
        reader.readCSV(new File("instances/test.csv"));
        List<Customer> customers = reader.getCustomers();
        Distance distance = new Euclidean(customers.stream().map(Customer::getLocation).collect(Collectors.toList()));
        int p = 3;
        int m = 3;
        int[] r = customers.stream().mapToInt(Customer::getReleaseDate).toArray();
        int[] d = customers.stream().mapToInt(c -> c.getReleaseDate() + c.getDays() - 1).toArray();
        MPBPMProblemVNDS vnds = new MPBPMProblemVNDS(customers.size(), p, m, distance.getDistancesMatrix(), r, d);
        vnds.run();
        System.out.println("DONE: " + vnds.getObjective());
    }
}
