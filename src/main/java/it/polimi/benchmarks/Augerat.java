package it.polimi.benchmarks;

import it.polimi.algorithms.vrp.VRPALNS;
import it.polimi.algorithms.vrp.constraints.CapacityConstraint;
import it.polimi.algorithms.vrp.objectives.TotalDistance;
import it.polimi.algorithms.vrp.recreate.GreedyInsertion;
import it.polimi.domain.Location;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;
import it.polimi.io.AugeratReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Augerat {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        String baseInstancePath = "instances/augerat/A-VRP/";
        String baseSolutionPath = "instances/augerat/A-VRP-sol/";

        File baseSolutionDir = new File(baseSolutionPath);
        String[] solutionPathNames = baseSolutionDir.list();

        assert solutionPathNames != null;

        String[] nam = new String[solutionPathNames.length];
        double[] opt = new double[solutionPathNames.length];
        double[] sol = new double[solutionPathNames.length];
        double[] gap = new double[solutionPathNames.length];
        double[] tim = new double[solutionPathNames.length];

        int k = 10;
        for (int i=0; i<solutionPathNames.length; i++) {
            String solutionName = solutionPathNames[i];
            String[] nameComponents = solutionName.split("-");
            String instanceName = nameComponents[1] + "-" + nameComponents[2] + "-" + nameComponents[3];
            int numVehicles = Integer.parseInt(nameComponents[3].substring(1));
            String solutionPath = baseSolutionPath + solutionName;
            String instancePath = baseInstancePath + instanceName + ".vrp";

            double optimal = getOpt(solutionPath);
            double solutionSum = 0.;
            double timeSum = 0.;
            for (int j=0; j<k; j++) {
                long start = System.nanoTime();
                solutionSum += solve(instancePath, numVehicles, executorService);
                long end = System.nanoTime();
                timeSum += (end - start)/1e6;
            }
            double solution = solutionSum/k;
            double time = timeSum/k;

            nam[i] = instanceName;
            opt[i] = optimal;
            sol[i] = solution;
            gap[i] = 100 * (solution - optimal) / optimal;
            tim[i] = time;
        }

        String formatRow = "%-12s \t %-12s \t %-12s \t %-12s%n";

        System.out.println(String.format(formatRow, "Name", "Optimal", "Heuristic", "Gap", "Time"));
        for (int i=0; i<solutionPathNames.length; i++)
            System.out.println(String.format(formatRow, nam[i], opt[i], sol[i], gap[i] + "%", tim[i]));
    }

    private static double solve(String instancePath, int numVehicles, ExecutorService executorService) {
        AugeratReader reader = new AugeratReader(instancePath, numVehicles);
        VehicleRoutingProblem vrp = reader.getVrp();
        vrp.setVRPObjectiveFunction(new TotalDistance(vrp, 1e6));
        vrp.addConstraint(new CapacityConstraint());
        //vrp.addConstraint(new MaxServiceTimeConstraint(8*60*60, vrp));
        VehicleRoutingProblemSolution initial = getInitialSolution(executorService, vrp);

        VRPALNS vrpalns = VRPALNS.getBuilder()
                .setEps(0.4)
                .setP(6).setChi(3).setPhi(9).setPsi(2)
                .setPworst(3)
                .setTstart(getTStart(vrp, initial)).setC(0.99975)
                .setD1(33).setD2(9).setD3(13).setR(0.1)
                .setMaxNumSegments(100).setSegmentSize(100)
                .setMaxN(getMaxN(vrp))
                .build();

        VehicleRoutingProblemSolution solution = (VehicleRoutingProblemSolution) vrpalns.run(vrp, initial);
        return solution.getCost();
    }

    private static VehicleRoutingProblemSolution getInitialSolution(ExecutorService executorService, VehicleRoutingProblem vrp) {
        List<VehicleRoute> emptyRoutes = vrp.getVehicles().stream()
                .map(v -> new VehicleRoute(new ArrayList<>(), v, 0., 0,0))
                .collect(Collectors.toList());
        VehicleRoutingProblemSolution initial = new VehicleRoutingProblemSolution(emptyRoutes, vrp.getJobs(), 0.);
        GreedyInsertion constructionHeuristic = new GreedyInsertion(vrp, executorService);
        initial = (VehicleRoutingProblemSolution) constructionHeuristic.recreate(initial);
        double cost = vrp.getVRPObjectiveFunction().getCost(initial);
        initial.setCost(cost);
        return initial;
    }

    private static double getOpt(String solutionPath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(solutionPath));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                String[] splitted = line.split(" ");
                if (splitted.length > 0 && splitted[0].equals("cost") || splitted[0].equals("Cost"))
                    return Double.parseDouble(splitted[1]);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Opt cost not found for " + solutionPath);
    }

    private static double getTStart(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution vrps) {
        double cost = 0.;
        double w = 0.05;
        for (VehicleRoute vr : vrps.getRoutes()) {
            Location cur = vrp.getDepot().getLocation(), prev;
            for (Job job : vr.getJobs()) {
                prev = cur;
                cur = job.getLocation();
                cost += vrp.getDistance().distance(prev, cur);
            }
            cost += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
        }
        return w*cost / Math.log(2);
    }

    private static double getMaxN(VehicleRoutingProblem vrp) {
        double eta = 0.025;
        double maxDist = Double.MIN_VALUE;
        float[][] dists = vrp.getDistance().getDistancesMatrix();
        for (int i=0; i<dists.length; i++) {
            for (int j=0; j<dists[i].length; j++) {
                if (dists[i][j] > maxDist)
                    maxDist = dists[i][j];
            }
        }
        return eta*maxDist;
    }

}
