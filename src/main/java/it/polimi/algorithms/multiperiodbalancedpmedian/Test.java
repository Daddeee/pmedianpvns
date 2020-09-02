package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Customer;
import it.polimi.io.TestCSVReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        TestCSVReader reader = new TestCSVReader();
        reader.readCSV(new File("instances/test.csv"));
        List<Customer> customers = reader.getCustomers();
        Distance distance = new Euclidean(customers.stream().map(Customer::getLocation).collect(Collectors.toList()));
        int p = 3;
        int m = 3;

        System.out.println("=============== EXACT STANDALONE ===============");
        String modelPath = "models/tdp/multi-period-balanced-p-median.mod";
        String resultPath = "instances/test-exact.csv";
        MultiPeriodBalancedPMedianExact exactOnly = new MultiPeriodBalancedPMedianExact(modelPath, resultPath, customers,
                distance, p, m);
        exactOnly.run();

        System.out.println("====================== VNS ======================");
        String resultPathVNS = "instances/test-vns.csv";
        int kmax = Math.max(customers.size() / 10, 5);
        MultiPeriodBalancedPMedianVNS vns = new MultiPeriodBalancedPMedianVNS(resultPathVNS, customers, Euclidean.class,
                p, m, kmax);
        vns.run();
        /*
        System.out.println("=============== HEURISTIC + EXACT ===============");
        String resultPath1 = "instances/test-hybrid.csv";
        ConstructionHeuristic ch = new ConstructionHeuristic(services, Euclidean.class, p, m);
        ch.run();
        MultiPeriodBalancedPMedianExact exactAndCh = new MultiPeriodBalancedPMedianExact(modelPath, resultPath1, services, p,
                m, getAlpha(services));
        exactAndCh.run(ch.getPeriods(), ch.getMedians(), ch.getSupermedians());
        */
    }
}
