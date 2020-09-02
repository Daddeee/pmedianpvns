package it.polimi.algorithms.vrp.recreate;

import it.polimi.algorithms.alns.RecreateOperator;
import it.polimi.algorithms.vrp.constraints.JobConstraint;
import it.polimi.algorithms.vrp.constraints.RouteConstraint;
import it.polimi.distances.Distance;
import it.polimi.domain.Solution;
import it.polimi.domain.vrp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class RegretInsertion implements RecreateOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegretInsertion.class);
    private VehicleRoutingProblem vrp;
    private ExecutorService executorService;

    public RegretInsertion(VehicleRoutingProblem vrp, ExecutorService executorService) {
        this.vrp = vrp;
        this.executorService = executorService;
    }

    @Override
    public Solution recreate(Solution s) {
        Distance d = vrp.getDistance();
        VehicleRoutingProblemSolution vrps = ((VehicleRoutingProblemSolution) s).clone();
        Map<Job, Insertion> bestInsertions = new HashMap<>();
        Map<Job, Insertion> secondBestInsertions = new HashMap<>();
        int insertionsCount = 0;

        //LOGGER.info("Inserting " + vrps.getUnassignedJobs().size() + " jobs.");
        while(vrps.getUnassignedJobs().size() > 0) {
            for (Job toInsert : vrps.getUnassignedJobs()) {
                if (bestInsertions.containsKey(toInsert) && secondBestInsertions.containsKey(toInsert))
                    continue;

                Insertion bestInsertion = Insertion.NO_INSERTION;
                Insertion secondBestInsertion = Insertion.NO_INSERTION;
                for (VehicleRoute vehicleRoute : vrps.getRoutes()) {

                    boolean satisfiesRouteConstraints = true;
                    for (RouteConstraint routeConstraint : vrp.getRouteConstraints()) {
                        if (routeConstraint.isViolated(toInsert, vehicleRoute, vrp)) {
                            satisfiesRouteConstraints = false;
                            break;
                        }
                    }
                    if (!satisfiesRouteConstraints)
                        continue;

                    Job prev, next = vrp.getDepot(), cur;
                    cur = toInsert;
                    List<Job> routeJobs = vehicleRoute.getJobs();
                    for (int i=0; i<routeJobs.size(); i++) {
                        prev = next;
                        next = routeJobs.get(i);

                        boolean satisfiesJobConstraints = true;
                        for (JobConstraint jobConstraint : vrp.getJobConstraints()) {
                            if (jobConstraint.isViolated(cur, prev, next, vehicleRoute, vrp)) {
                                satisfiesJobConstraints = false;
                                break;
                            }
                        }
                        if (!satisfiesJobConstraints)
                            continue;

                        double delta = calculateInsertionDelta(prev, next, cur, vrps);

                        Insertion insertion = new Insertion(toInsert, vehicleRoute, i, delta, vrp);
                        if (insertion.isBetterThan(bestInsertion)) {
                            secondBestInsertion = bestInsertion;
                            bestInsertion = insertion;
                        } else if (insertion.isBetterThan(secondBestInsertion)) {
                            secondBestInsertion = insertion;
                        }
                    }

                    prev = next;
                    next = vrp.getDepot();
                    double delta = calculateInsertionDelta(prev, next, cur, vrps);

                    boolean satisfiesJobConstraints = true;
                    for (JobConstraint jobConstraint : vrp.getJobConstraints()) {
                        if (jobConstraint.isViolated(cur, prev, next, vehicleRoute, vrp)) {
                            satisfiesJobConstraints = false;
                            break;
                        }
                    }
                    if (satisfiesJobConstraints) {
                        Insertion insertion = new Insertion(toInsert, vehicleRoute, routeJobs.size(), delta, vrp);
                        if (insertion.isBetterThan(bestInsertion)) {
                            secondBestInsertion = bestInsertion;
                            bestInsertion = insertion;
                        } else if (insertion.isBetterThan(secondBestInsertion)) {
                            secondBestInsertion = insertion;
                        }
                    }
                }

                bestInsertions.put(toInsert, bestInsertion);
                secondBestInsertions.put(toInsert, secondBestInsertion);
            }

            Insertion bestInsertionOverall = Insertion.NO_INSERTION;
            double bestRegret = Double.MIN_VALUE;
            for (Job job : bestInsertions.keySet()) {
                Insertion bestInsertion = bestInsertions.get(job);
                Insertion secondBestInsertion = secondBestInsertions.get(job);
                double regret = secondBestInsertion.getDelta() - bestInsertion.getDelta();
                if (regret > bestRegret) {
                    bestRegret = regret;
                    bestInsertionOverall = bestInsertion;
                }
            }

            if (bestInsertionOverall == Insertion.NO_INSERTION) {
                //LOGGER.info("Insertions incomplete. Inserted " + insertionsCount + " jobs.");
                return vrps;
            }

            // IMPORTANT: repeat calculation because insertion's delta could be different from objective function's
            // delta, for example if adding random noise when selecting insertions.
            double insertionCost = calculateInsertionDelta(bestInsertionOverall, vrps);

            bestInsertionOverall.perform();
            insertionsCount++;
            vrps.setCost(vrps.getCost() + insertionCost - vrp.getObjectiveFunction().getUnassignedPenalty());
            vrps.getUnassignedJobs().remove(bestInsertionOverall.getJob());
            bestInsertions.clear();
            secondBestInsertions.clear();
        }
        //LOGGER.info("Insertions complete. Inserted " + insertionsCount + " jobs.");
        return vrps;
    }

    protected double calculateInsertionDelta(Job prev, Job next, Job cur, VehicleRoutingProblemSolution vrps) {
        return vrp.getObjectiveFunction().getDelta(cur, prev, next, vrps);
    }

    protected double calculateInsertionDelta(Insertion insertion, VehicleRoutingProblemSolution vrps) {
        int position = insertion.getPosition();
        List<Job> jobs = insertion.getVehicleRoute().getJobs();
        Job prev = (position == 0) ? vrp.getDepot() : jobs.get(position - 1);
        Job next = (position == jobs.size()) ? vrp.getDepot() : jobs.get(position);
        return vrp.getObjectiveFunction().getDelta(insertion.getJob(), prev, next, vrps);
    }
}
