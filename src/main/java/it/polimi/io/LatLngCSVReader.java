package it.polimi.io;

import it.polimi.domain.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LatLngCSVReader {
    public static List<Location> read(String filepath) {
        BufferedReader reader;
        List<Location> locations = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                String[] splitted = line.split(",");

                double lat = Double.parseDouble(splitted[0]);
                double lng = Double.parseDouble(splitted[1]);

                if (lat != 0 || lng != 0) {
                    String id = (splitted.length == 3) ? splitted[2] : Integer.toString(count++);
                    locations.add(new Location(id, lat, lng));
                }

                line = reader.readLine();
            }
            reader.close();
            return locations;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
