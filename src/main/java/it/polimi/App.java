package it.polimi;

import it.polimi.algorithm.VNS;
import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;

import java.util.List;

public class App {
    public static void main( String[] args ) {
        List<Location> locations = LatLngCSVReader.read("input.csv");
        //Distance distance = new OSRM(locations);
        Distance distance = new Haversine(locations);

        VNS vns = new VNS();
        vns.run(locations.size(), 5, distance.getDurationsMatrix(), 5);

        System.out.print("Medians: ");
        int[] medians = vns.getMedians();
        for (int i=0; i<medians.length; i++) {
            System.out.print(medians[i]);
            if (i < medians.length - 1) {
                System.out.print(", ");
            } else {
                System.out.println();
            }
        }

        System.out.println("Objective: " + vns.getObjective());
    }
}
