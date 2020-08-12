package it.polimi.io;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.utils.FloydWarshall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TSPLIBReader {
    private int n;
    private int p;
    private float[][] d;

    public TSPLIBReader(String filepath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));

            reader.readLine(); // NAME
            reader.readLine(); // COMMENT
            reader.readLine(); // TYPE

            String line = reader.readLine(); // DIMENSION
            String[] splitted = line.split(" : ");
            n = Integer.parseInt(splitted[1]);
            p = 10; // iteratively try different p

            line = reader.readLine(); // EDGE_WEIGHT_TYPE
            splitted = line.split(" : ");
            String distanceType = splitted[1];

            reader.readLine(); // NODE_COORD_SECTION

            List<Location> locations = new ArrayList<>();
            line = reader.readLine();
            line = line.trim();
            while (line != null && line.length() > 0 && !line.equals("EOF")) {
                splitted = line.split(" ");
                double lat = Double.parseDouble(splitted[1]);
                double lng = Double.parseDouble(splitted[2]);
                locations.add(new Location(splitted[0], lat, lng));
                line = reader.readLine();
                line = line.trim();
            }
            reader.close();

            d = getDistance(distanceType, locations);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getN() {
        return n;
    }

    public int getP() {
        return p;
    }

    public float[][] getD() {
        return d;
    }

    private float[][] getDistance(String distanceType, List<Location> locations) {
        Distance d;
        switch (distanceType) {
            case "EUC_2D":
                d = new Euclidean(locations);
                break;
            case "GEO":
                d = new Haversine(locations);
                break;
            default:
                throw new IllegalStateException("Unknown distance type: " + distanceType);
        }

        return d.getDurationsMatrix();
    }
}
