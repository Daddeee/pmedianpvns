package it.polimi.io;

import it.polimi.domain.Location;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ZonesCSVWriter {

    public static void write(String filepath, Location[] locations, int[] labels) {
        if (locations.length != labels.length)
            throw new IllegalArgumentException("Locations and labels have different sizes: " +
                    locations.length + " and " + labels.length);

        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%f,%f,%d\n";

            for (int i=0; i<locations.length; i++)
                writer.write(String.format(formatRow, locations[i].getLat(), locations[i].getLng(), labels[i]));

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
