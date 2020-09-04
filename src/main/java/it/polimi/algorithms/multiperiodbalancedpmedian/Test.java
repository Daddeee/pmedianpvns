package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.algorithms.mpbpmp.MPBPMProblemVNDS;
import it.polimi.algorithms.mpbpmp.domain.MPBPMPSolution;
import it.polimi.algorithms.mpbpmp.domain.MPBPMProblem;
import it.polimi.algorithms.mpbpmp.domain.Service;
import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Customer;
import it.polimi.io.TestCSVReader;
import it.polimi.utils.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        TestCSVReader reader = new TestCSVReader();
        reader.readCSV(new File("instances/test.csv"));
        List<Customer> customers = reader.getCustomers();
        List<Service> services = customers.stream()
                .map(c -> new Service(c.getLocation().getId(), c.getLocation(), c.getReleaseDate(),
                        c.getReleaseDate() + c.getDays() - 1))
                .collect(Collectors.toList());
        Distance distance = new Euclidean(customers.stream().map(Customer::getLocation).collect(Collectors.toList()));
        int p = 2;
        int m = 2;


        System.out.println("=============== EXACT STANDALONE ===============");
        String modelPath = "models/tdp/multi-period-balanced-p-median.mod";
        String resultPath = "instances/test-exact.csv";
        MultiPeriodBalancedPMedianExact exactOnly = new MultiPeriodBalancedPMedianExact(modelPath, resultPath, customers,
                distance, p, m);
        exactOnly.run();

        System.out.println("====================== VNDS ======================");
        String resultPathVNS = "instances/test-vns.csv";
        MPBPMProblem problem = new MPBPMProblem(services, m, p, distance);
        MPBPMProblemVNDS vnds = new MPBPMProblemVNDS(1337);
        MPBPMPSolution solution = vnds.run(problem);
        logResults(solution);
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

    private static void logResults(MPBPMPSolution solution) {
        Map<Integer, Integer> medianCounts = solution.getPointsPerMedian().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        System.out.println("vnds solution: (obj=" + solution.getObjective() + ", time=" + solution.getElapsedTime() + ")");
        String title = "medians count";
        printCounts(medianCounts, title);
        printIndexes(medianCounts.keySet(), Arrays.stream(solution.getPeriods()).boxed().collect(Collectors.toList()), "median periods");
    }

    private static <T> void printIndexes(Collection<Integer> indexes, List<T> arr, String title) {
        System.out.print(title + ": (");
        String body = indexes.stream()
                .map(i -> new Pair(i, arr.get(i)))
                .map(p -> p.getFirst() + " => " + p.getSecond())
                .collect(Collectors.joining(", "));
        System.out.print(body);
        System.out.println(")");
    }

    private static void printCounts(final Map<Integer, Integer> counts, final String title) {
        System.out.print(title + ": (");
        boolean first = true;
        List<Integer> keys = counts.keySet().stream().sorted().collect(Collectors.toList());
        for (Integer k : keys) {
            String s = first ? k + " => " + counts.get(k) : ", " + k + " => " + counts.get(k);
            first = false;
            System.out.print(s);
        }
        System.out.println(")");
    }

    private static Map<Integer, Integer> getCounts(final int[] labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<labels.length; i++) {
            if (labels[i] == -1) continue;
            int cnt = counts.getOrDefault(labels[i], 0);
            counts.put(labels[i], cnt+1);
        }
        return counts;
    }
}
