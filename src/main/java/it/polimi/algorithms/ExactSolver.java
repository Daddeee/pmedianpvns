package it.polimi.algorithms;

import com.ampl.AMPL;
import com.ampl.Parameter;
import com.ampl.Tuple;

import java.io.IOException;

public abstract class ExactSolver {

    protected String modelPath;
    protected double objective;
    protected double elapsedTime;

    public ExactSolver(String modelPath) {
        this.modelPath = modelPath;
    }

    public void run() {
        AMPL ampl = new AMPL();
        try {
            loadModel(ampl);
            loadParams(ampl);
            loadInitialSolution(ampl);
            long start = System.nanoTime();
            ampl.solve();
            long end = System.nanoTime();
            this.elapsedTime = (end - start) / 1e6;
            this.objective = ampl.getObjective(ampl.getCurrentObjectiveName()).value();
            getResults(ampl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
    }

    protected abstract void getResults(AMPL ampl);

    protected abstract void loadParams(AMPL ampl);

    protected void loadInitialSolution(AMPL ampl) {
        System.out.println("No initial solution provided.");
    }

    protected void loadModel(AMPL ampl) throws IOException {
        ampl.read(this.modelPath);
        ampl.readData("");
        ampl.setOption("solver", "cplex");
        ampl.setOption("cplex_options", "threads=1");
    }

    protected void setMatrix(Parameter param, int[][] matrix) {
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

    protected void setMatrix(Parameter param, float[][] matrix) {
        int r = matrix.length, c = -1;
        for (int i=0; i<r; i++)
            c = Math.max(c, matrix[i].length);
        Tuple[] tuples = new Tuple[r*c];
        double[] values = new double[r*c];
        int k = 0;
        for (int i=0; i<r; i++) {
            for (int j=0; j<c; j++) {
                tuples[k] = new Tuple(i, j);
                values[k] = matrix[i][j];
                k++;
            }
        }
        param.setValues(tuples, values);
    }
}
