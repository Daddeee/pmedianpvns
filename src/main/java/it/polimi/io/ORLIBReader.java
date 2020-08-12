package it.polimi.io;

import it.polimi.utils.FloydWarshall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ORLIBReader {
    private int n;
    private int p;
    private float[][] d;

    public ORLIBReader(String filepath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));

            String line = reader.readLine();
            line = line.trim();
            String[] splitted = line.split(" ");
            n = Integer.parseInt(splitted[0]);
            p = Integer.parseInt(splitted[2]);

            float[][] adj = new float[n][n];
            for (int i=0; i<n; i++) {
                for (int j=0; j<n; j++) {
                    adj[i][j] = (i == j) ? 0 : Float.MAX_VALUE;
                }
            }

            line = reader.readLine();
            while (line != null && line.length() > 0) {
                line = line.trim();
                splitted = line.split(" ");
                int from = Integer.parseInt(splitted[0]) - 1;
                int to = Integer.parseInt(splitted[1]) - 1;
                float w = Float.parseFloat(splitted[2]);
                adj[from][to] = w;
                adj[to][from] = w;
                line = reader.readLine();
            }
            reader.close();

            d = FloydWarshall.compute(adj);

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
}
