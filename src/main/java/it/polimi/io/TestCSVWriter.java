package it.polimi.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestCSVWriter {

    private int k;
    private int[] n;
    private int[] p;
    private float[] optima;
    private float[] results;
    private double[] times;
    private float[] errors;

    public TestCSVWriter(final int n[], final int p[], final float[] optima, final float[] results,
                         final double[] times) {
        assert n.length == p.length && p.length == optima.length && optima.length == results.length
                && results.length == times.length;

        this.k = optima.length;
        this.n = n;
        this.p = p;
        this.optima = optima;
        this.results = results;
        this.times = times;
        this.errors = new float[k];
        for (int i=0; i<k; i++) {
            float diff = results[i] - optima[i];
            errors[i] = 100*diff/optima[i];
        }
    }

    public void write(String filepath) {
        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%-10s %-10s %-10s %-10s %-10s %-10s%n";

            writer.write(String.format(formatRow, "N", "P", "Opt", "Res", "Err", "Time"));

            for (int i=0; i<k; i++) {
                writer.write(String.format(formatRow, Integer.toString(n[i]), Integer.toString(p[i]),
                        String.format("%.2f", optima[i]), String.format("%.2f", results[i]),
                        String.format("%.2f%%", errors[i]), String.format("%.2fms", times[i])));
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
