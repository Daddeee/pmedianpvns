package it.polimi.benchmarks;

import it.polimi.algorithm.VNS;
import it.polimi.io.ORLIBReader;
import it.polimi.io.TestCSVWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ORLIB {

    private static float[] readopt() {
        try {
            float[] opt = new float[40];
            BufferedReader reader = new BufferedReader(new FileReader("instances/orlib/pmedopt.txt"));
            reader.readLine();
            String line = reader.readLine();
            int count = 0;
            while (line != null && line.length() > 0) {
                line = line.trim();
                String[] splitted = line.split("\\s+");
                opt[count] = Float.parseFloat(splitted[1]);
                count++;
                line = reader.readLine();
            }
            reader.close();
            return opt;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String basePath = "instances/orlib/pmed%d.txt";
        float[] opt = readopt();
        int[] n = new int[opt.length];
        int[] p = new int[opt.length];
        float[] res = new float[opt.length];
        double[] times = new double[opt.length];

        for (int i=1; i<=40; i++) {
            String path = String.format(basePath, i);
            ORLIBReader orlibReader = new ORLIBReader(path);
            VNS vns = new VNS();

            System.out.print(path);
            long start = System.nanoTime();

            vns.run(orlibReader.getN(), orlibReader.getP(), orlibReader.getD(), orlibReader.getP());

            long end = System.nanoTime();
            double time = (end - start) / 1e6;
            System.out.println(String.format(" res=%.2f time=%.2fms", vns.getObjective(), time));

            n[i-1] = orlibReader.getN();
            p[i-1] = orlibReader.getP();
            res[i-1] = vns.getObjective();
            times[i-1] = time;
        }

        TestCSVWriter writer = new TestCSVWriter(n, p, opt, res, times);
        writer.write("instances/results/vns-orlib.csv");
    }

}
