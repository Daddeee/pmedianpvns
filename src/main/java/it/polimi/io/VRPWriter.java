package it.polimi.io;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoute;
import it.polimi.domain.routing.VehicleRoutingProblem;
import it.polimi.domain.routing.VehicleRoutingProblemSolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VRPWriter {

    public static void write(String filePath, VehicleRoutingProblemSolution solution, VehicleRoutingProblem problem,
                             Map<String, Integer> mediansMap) {
        try {
            File solutionFile = new File(filePath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            for (VehicleRoute route : solution.getRoutes()) {
                writer.write("Route " + route.getVehicle().getId() + "\n");
                writer.write(problem.getDepot().getLocation().getLat() + "," +
                        problem.getDepot().getLocation().getLng() + "," + mediansMap.get(problem.getDepot().getId()) + "\n");
                for (Job job : route.getJobs())
                    writer.write(job.getLocation().getLat() + "," + job.getLocation().getLng() + "," +
                            mediansMap.get(job.getId()) + "\n");
                writer.write(problem.getDepot().getLocation().getLat() + "," +
                        problem.getDepot().getLocation().getLng() + "," + mediansMap.get(problem.getDepot().getId()) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String filePath, VehicleRoutingProblemSolution solution, VehicleRoutingProblem problem) {
        Map<String, Integer> mediansMap = new HashMap<>();
        mediansMap.put(problem.getDepot().getId(), 0);
        problem.getJobs().stream().forEach(j -> mediansMap.put(j.getId(), 0));
        VRPWriter.write(filePath, solution, problem, mediansMap);
    }

}
