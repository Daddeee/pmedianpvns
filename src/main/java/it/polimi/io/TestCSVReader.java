package it.polimi.io;

import it.polimi.domain.Location;
import it.polimi.domain.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCSVReader {

    public List<Service> readCSV(File csv) {
        List<Service> services = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(csv));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                String[] splitted = line.split(",");
                if (splitted.length < 5) break;
                double x = Double.parseDouble(splitted[0]);
                double y = Double.parseDouble(splitted[1]);
                Location loc = new Location(Integer.toString(count), x, y);
                int r = Integer.parseInt(splitted[2]);
                int days = Integer.parseInt(splitted[3]);
                int score = Integer.parseInt(splitted[4]);
                services.add(new Service(loc, r, days, score));
                line = reader.readLine();
                count++;
            }
            reader.close();
            return services;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
