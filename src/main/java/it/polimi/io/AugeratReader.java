package it.polimi.io;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Location;
import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.Vehicle;
import it.polimi.domain.routing.VehicleRoutingProblem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AugeratReader {

    private VehicleRoutingProblem vrp;

    public VehicleRoutingProblem getVrp() {
        return vrp;
    }

    public AugeratReader(String filepath, int numVehicles) {
        try {
            Job depot;
            List<Job> jobs;
            List<Vehicle> vehicles;
            Distance distance;

            BufferedReader reader = new BufferedReader(new FileReader(filepath));

            reader.readLine(); // NAME
            reader.readLine(); // COMMENT
            reader.readLine(); // TYPE
            reader.readLine(); // DIMENSION
            reader.readLine(); // EDGE_WEIGHT_TYPE

            String line = reader.readLine(); // CAPACITY
            String[] splitted = line.split(" : ");
            int capacity = Integer.parseInt(splitted[1]);
            vehicles = IntStream.range(0, numVehicles).mapToObj(i -> new Vehicle("" + i, capacity))
                    .collect(Collectors.toList());

            reader.readLine(); // NODE_COORD_SECTION

            List<Location> locations = new ArrayList<>();
            line = reader.readLine();
            line = line.trim();
            while (line.length() > 0 && !line.equals("DEMAND_SECTION")) {
                splitted = line.split(" ");
                double lat = Double.parseDouble(splitted[1]);
                double lng = Double.parseDouble(splitted[2]);
                locations.add(new Location(splitted[0], lat, lng));
                line = reader.readLine();
                line = line.trim();
            }
            distance = new Euclidean(locations);

            jobs = new ArrayList<>();
            line = reader.readLine();
            line = line.trim();
            while(line.length() > 0 && !line.equals("DEPOT_SECTION")) {
                splitted = line.split(" ");
                int idx = Integer.parseInt(splitted[0]);
                int size = Integer.parseInt(splitted[1]);
                Location l = locations.get(idx - 1);
                jobs.add(new Job("" + idx, l, size, 0.));
                line = reader.readLine();
                line = line.trim();
            }
            line = reader.readLine();
            line = line.trim();

            int depotIdx = Integer.parseInt(line) - 1;
            depot = jobs.remove(depotIdx);

            reader.close();

            this.vrp = new VehicleRoutingProblem(depot, jobs, vehicles, distance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
