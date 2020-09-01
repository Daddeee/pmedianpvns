package it.polimi.algorithms.multiperiodbalancedpmedian;

import com.ampl.*;

public class HighLevelPMedian {

    private int n;
    private int p;
    private int m;
    private float[][] c;
    private int[][] t;

    public HighLevelPMedian(final int n, final int p, final int m, final float[][] c, final int[][] t) {
        this.n = n;
        this.p = p;
        this.m = m;
        this.c = c;
        this.t = t;
    }

    public int[] run() {
        AMPL ampl = new AMPL();
        try {

            ampl.read("models/tdp/high-level-p-median.mod");
            ampl.readData("");
            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=1");
            //ampl.setOption("cplex_options", "threads=1 iisfind=1");

            Parameter nn = ampl.getParameter("n");
            nn.setValues(n);

            Parameter pp = ampl.getParameter("p");
            pp.setValues(p);

            Parameter mm = ampl.getParameter("m");
            mm.setValues(m);

            Parameter cc = ampl.getParameter("c");
            setMatrix(cc, c);

            Parameter tt = ampl.getParameter("t");
            setMatrix(tt, t);

            long start = System.nanoTime();

            ampl.solve();

            //ampl.eval("display _varname, _var.iis, _conname, _con.iis;");

            long end = System.nanoTime();
            double elapsed_time = (end - start) / 1e6;
            System.out.println("HighLevelPMedian elapsed time: " + elapsed_time);

            return getX(ampl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
        return null;
    }

    private int[] getX(AMPL ampl) {
        int[] x = new int[n];
        // Get the values of the variable Buy in a dataframe object
        Variable xx = ampl.getVariable("x");
        DataFrame xdf = xx.getValues();
        xdf.iterator().forEachRemaining(os -> {
            if ((double) os[2] == 1.)
                x[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });

        return x;
    }

    private void setMatrix(Parameter param, float[][] matrix) {
        Tuple[] tuples = new Tuple[matrix.length*matrix.length];
        double[] values = new double[matrix.length*matrix.length];
        int k = 0;
        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                tuples[k] = new Tuple(i, j);
                values[k] = matrix[i][j];
                k++;
            }
        }
        param.setValues(tuples, values);
    }

    private void setMatrix(Parameter param, int[][] matrix) {
        int r = matrix.length, c = -1;
        for (int i=0; i<r; i++)
            c = Math.max(c, matrix[i].length);

        Tuple[] tuples = new Tuple[r*c];
        double[] values = new double[r*c];
        int k = 0;
        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                tuples[k] = new Tuple(i, j);
                values[k] = matrix[i][j];
                k++;
            }
        }
        param.setValues(tuples, values);
    }
}
