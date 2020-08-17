package it.polimi.benchmarks;

import it.polimi.algorithm.VNS;
import it.polimi.io.TSPLIBReader;
import it.polimi.io.TestCSVWriter;

import java.util.Arrays;

public class TSPLIB {

    public static void main(String[] args) {
        String basePath = "instances/tsplib/";
        String[] datasets = { "fl1400.tsp", "pcb3038.tsp", "rl5934.tsp", "rl11849.tsp" };
        int[][] ps = {
                { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 },
                { 50, 100, 150, 200, 250, 300, 350, 400, 450, 500 },
                { 50, 100, 150, 200, 250, 300, 350, 400, 450, 500 },
                { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 }
        };

        int k = ps.length*ps[0].length;
        int[] n = new int[k];
        int[] p = new int[k];
        float[] opt = new float[k];
        Arrays.fill(opt, -1);
        float[] res = new float[k];
        double[] times = new double[k];

        int count = 0;
        for (int i=0; i<datasets.length; i++) {
            String datasetPath = basePath + datasets[i];

            TSPLIBReader reader = new TSPLIBReader(datasetPath);

            for (int j=0; j<ps[0].length; j++) {
                VNS vns = new VNS(reader.getN(), ps[i][j], reader.getD());
                System.out.println(datasetPath);
                long start = System.nanoTime();
                vns.run();
                long end = System.nanoTime();
                double time = (end - start) / 1e6;
                System.out.println(String.format("res=%.2f time=%.2fms", vns.getObjective(), time));

                n[count] = reader.getN();
                p[count] = ps[i][j];
                res[count] = vns.getObjective();
                times[count] = time;

                count++;
            }
        }

        TestCSVWriter writer = new TestCSVWriter(n, p, opt, res, times);
        writer.write("instances/results/vns/tsplib.csv");
    }

}
