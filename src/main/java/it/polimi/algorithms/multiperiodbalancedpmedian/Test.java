package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Service;
import it.polimi.io.TestCSVReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        TestCSVReader reader = new TestCSVReader();
        List<Service> services = reader.readCSV(new File("instances/test.csv"));
        int p = 2;
        int m = 2;

        System.out.println("=============== EXACT STANDALONE ===============");
        String modelPath = "models/tdp/multi-period-balanced-p-median.mod";
        String resultPath = "instances/test-exact.csv";
        MultiPeriodBalancedPMedianExact exactOnly = new MultiPeriodBalancedPMedianExact(modelPath, resultPath, services, p,
                m, getAlpha(services));
        exactOnly.run();

        System.out.println("====================== VNS ======================");
        String resultPathVNS = "instances/test-vns.csv";
        int kmax = Math.max(services.size() / 10, 5);
        MultiPeriodBalancedPMedianVNS vns = new MultiPeriodBalancedPMedianVNS(resultPathVNS, services, Euclidean.class,
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

    private static float getAlpha(List<Service> services) {
        Distance dist = new Euclidean(services.stream().map(Service::getLocation).collect(Collectors.toList()));
        float[][] d = dist.getDurationsMatrix();
        int n = services.size();
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += d[i][j];
                    count += 1;
                }
            }
        }
        return 0.2f * distSum / count;
    }
}
