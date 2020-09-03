package it.polimi.algorithms.vrp.ruin;

import it.polimi.algorithms.alns.RuinOperator;
import it.polimi.domain.Location;
import it.polimi.domain.Solution;
import it.polimi.domain.vrp.Job;
import it.polimi.domain.vrp.VehicleRoute;
import it.polimi.domain.vrp.VehicleRoutingProblem;
import it.polimi.domain.vrp.VehicleRoutingProblemSolution;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ShawRemoval implements RuinOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShawRemoval.class);
    private final Random random;
    private final VehicleRoutingProblem vrp;
    private final int p;
    private final double eps;
    private final double phi;
    private final double chi;
    private final double psi;

    public ShawRemoval(VehicleRoutingProblem vrp, int p, double eps, double phi, double chi, double psi) {
        this.vrp = vrp;
        this.p = p;
        this.eps = eps;
        this.phi = phi;
        this.chi = chi;
        this.psi = psi;
        this.random = new Random();
    }

    public ShawRemoval(VehicleRoutingProblem vrp, int p, double eps, double phi, double chi, double psi, int seed) {
        this.vrp = vrp;
        this.p = p;
        this.eps = eps;
        this.phi = phi;
        this.chi = chi;
        this.psi = psi;
        this.random = new Random(seed);
    }

    @Override
    public VehicleRoutingProblemSolution ruin(Solution s) {
        VehicleRoutingProblemSolution vrps = (VehicleRoutingProblemSolution) s;
        List<Job> jobs = vrps.getRouteJobs();
        int q = 4 + random.nextInt(Math.min(96, (int) Math.floor(eps*Math.max(jobs.size() - 4, 0))));
        //LOGGER.info("Starting to remove " + q + " jobs.");
        List<Job> unassignedJobs = new ArrayList<>(Collections.singleton(Rand.sampleAndRemove(jobs, random)));
        for (int i=0; i<q-1; i++) {
            Job r = Rand.sample(unassignedJobs, random);
            jobs.sort(Comparator.comparingDouble(j -> relatedness(r, j)));
            int idx = (int) Math.floor(Math.pow(random.nextDouble(), p) * jobs.size());
            if (idx == jobs.size()) idx -= 1; // strange case
            unassignedJobs.add(jobs.remove(idx));
        }
        Set<Job> unassignedJobSet = new HashSet<>(unassignedJobs);
        double cost = 0.;
        List<VehicleRoute> newVehicleRoutes = new ArrayList<>();
        for (VehicleRoute vr : vrps.getRoutes()) {
            List<Job> newJobs = new ArrayList<>();
            Location prev, cur = vrp.getDepot().getLocation();
            double time = 0, distance = 0;
            int size = 0;
            for (Job j : vr.getJobs()) {
                if (!unassignedJobSet.contains(j)) {
                    prev = cur;
                    cur = j.getLocation();
                    time += vrp.getDistance().duration(prev, cur);
                    distance += vrp.getDistance().distance(prev, cur);
                    size += j.getSize();
                    cost += vrp.getDistance().distance(prev, cur);
                    newJobs.add(new Job(j.getId(), cur, j.getSize(), time));
                }
            }
            time += vrp.getDistance().duration(cur, vrp.getDepot().getLocation());
            cost += vrp.getDistance().distance(cur, vrp.getDepot().getLocation());
            newVehicleRoutes.add(new VehicleRoute(newJobs, vr.getVehicle(), time, distance, size));
        }
        unassignedJobs.addAll(vrps.getUnassignedJobs()); // include also previous unassigned jobs
        cost += unassignedJobs.size() * vrp.getVRPObjectiveFunction().getUnassignedPenalty();
        //LOGGER.info("Jobs removed. New cost: " + cost);
        return new VehicleRoutingProblemSolution(newVehicleRoutes, unassignedJobs, cost);
    }

    private double relatedness(Job i, Job j) {
        double dist = vrp.getDistance().distance(i.getLocation(), j.getLocation());
        double temp = Math.abs(i.getArrTime() - j.getArrTime());
        double load = Math.abs(i.getSize() - j.getSize());
        // no capacity and homogeneous fleet, no need for other 2 terms
        // TODO maybe there include zones
        return phi*dist + chi*temp + psi*load;
    }

}
