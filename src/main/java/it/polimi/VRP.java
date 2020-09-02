package it.polimi;

import it.polimi.algorithms.vrp.VRPALNS;
import it.polimi.algorithms.vrp.constraints.CapacityConstraint;
import it.polimi.algorithms.vrp.objectives.TotalDistance;
import it.polimi.algorithms.vrp.recreate.GreedyInsertion;
import it.polimi.domain.Location;
import it.polimi.domain.routing.*;
import it.polimi.io.AugeratReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class VRP {
    private static final Logger LOGGER = LoggerFactory.getLogger(VRP.class);

    public static void main(String [] args) {
        int numVehicles = 5;
        String filePath = "instances/augerat/A-VRP/A-n32-k5.vrp";
        ExecutorService executorService = Executors.newCachedThreadPool();
        AugeratReader reader = new AugeratReader(filePath, numVehicles);

        VehicleRoutingProblem vrp = reader.getVrp();
        VehicleRoutingProblemSolution solution = solve(executorService, vrp);

        for (VehicleRoute route : solution.getRoutes()) {
            String idxs = route.getJobs().stream().map(Job::getId).collect(Collectors.joining(" "));
            LOGGER.info(route.getVehicle().getId() + ": " + idxs);
        }
    }

    public static VehicleRoutingProblemSolution solve(ExecutorService executorService, VehicleRoutingProblem vrp) {
        vrp.setObjectiveFunction(new TotalDistance(vrp, 1e6));
        vrp.addConstraint(new CapacityConstraint());
        //vrp.addConstraint(new MaxServiceTimeConstraint(8*60*60, vrp));

        VehicleRoutingProblemSolution initial = getInitialSolution(executorService, vrp);

        LOGGER.info("Initial solution, obj=" + initial.getCost());

        VRPALNS vrpalns = VRPALNS.getBuilder()
                .setEps(0.4)
                .setP(6).setChi(3).setPhi(9).setPsi(2)
                .setPworst(3)
                .setTstart(getTStart(vrp, initial)).setC(0.99975)
                .setD1(33).setD2(9).setD3(13).setR(0.1)
                .setMaxNumSegments(10).setSegmentSize(50)
                .setMaxN(getMaxN(vrp))
                .setSeed(1337)
                .build();

        VehicleRoutingProblemSolution solution = (VehicleRoutingProblemSolution) vrpalns.run(vrp, initial);

        LOGGER.info("Final solution obj=" + solution.getCost());
        return solution;
    }

    private static VehicleRoutingProblemSolution getInitialSolution(ExecutorService executorService, VehicleRoutingProblem vrp) {
        List<VehicleRoute> emptyRoutes = vrp.getVehicles().stream()
                .map(v -> new VehicleRoute(new ArrayList<>(), v, 0., 0, 0))
                .collect(Collectors.toList());
        VehicleRoutingProblemSolution initial = new VehicleRoutingProblemSolution(emptyRoutes, vrp.getJobs(), 0.);
        GreedyInsertion constructionHeuristic = new GreedyInsertion(vrp, executorService);
        initial = (VehicleRoutingProblemSolution) constructionHeuristic.recreate(initial);
        double cost = vrp.getObjectiveFunction().getCost(initial);
        initial.setCost(cost);
        return initial;
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
