package it.polimi.algorithms.mpbpmp;

import com.google.common.collect.Lists;
import it.polimi.algorithms.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithms.mpbpmp.domain.MPBPMPSolution;
import it.polimi.algorithms.mpbpmp.domain.MPBPMProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MPBPMProblemVNDS {
    protected final int MAX_SOLUTION_CHANGES = 100;
    private final Logger LOGGER = LoggerFactory.getLogger(MPBPMProblemVNDS.class);
    private final Random random;

    public MPBPMProblemVNDS() {
        this(new Random());
    }

    public MPBPMProblemVNDS(int seed) {
        this(new Random(seed));
    }

    public MPBPMProblemVNDS(Random random) {
        this.random = random;
    }


    public MPBPMPSolution run(MPBPMProblem problem) {
        double start = System.nanoTime();

        MPBPMPSolution opt = getInitial(problem);
        LOGGER.info("Initial solution. opt=" + opt.getObjective());

        MPBPMPSolution cur = opt.clone();
        int k = 1;
        int changes = 0;
        List<Integer> shakable = IntStream.range(0, problem.getN())
                .filter(i -> problem.getD()[i] - problem.getR()[i] > 0).boxed().collect(Collectors.toList());
        while (k <= problem.getKmax()) {
            LOGGER.info("Exploring neighborhood of size k=" + k);
            Collections.shuffle(shakable, random);
            Set<Integer> touchedPeriods = new HashSet<>();
            // shaking
            for (int j = 0; j < k; j++) {
                if (shakable.size() <= 0) break;
                int toSwap = shakable.get(j);
                int oldPeriod = cur.getPeriods()[toSwap];
                // using random insertion
                int insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);
                while (insertionPeriod == cur.getPeriods()[toSwap])
                    insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);
                touchedPeriods.add(oldPeriod);
                touchedPeriods.add(insertionPeriod);
                // do not care about updating medians or objectives there
                cur.setPeriod(toSwap, insertionPeriod);
            }

            // local search
            for (int period : touchedPeriods) {
                LOGGER.info("Solving period " + period);
                solveSinglePeriod(cur, problem, period);
            }

            LOGGER.info("Done. cur=" + cur.getObjective() + ", opt=" + opt.getObjective());
            // Move or not
            if (cur.getObjective() < opt.getObjective()) {
                LOGGER.info("New improving solution founded. Moving.");
                opt = cur.clone();
                k = 1;
                changes++;
                if (changes >= MAX_SOLUTION_CHANGES) {
                    LOGGER.info("Max solution changes limit hit.");
                    break;
                }
            } else {
                cur = opt.clone();
                k = k + 1;
            }
        }
        double end = System.nanoTime();
        double elapsedTime = (end - start) / 1e6;
        opt.setElapsedTime(elapsedTime);
        return opt;
    }

    public MPBPMPSolution getInitial(MPBPMProblem problem) {
        int[] periods = new int[problem.getN()];
        int[] medians = new int[problem.getN()];
        int periodSize = (int) Math.ceil((double) problem.getN() / problem.getM());
        List<Integer> points = IntStream.range(0, problem.getN()).boxed().collect(Collectors.toList());
        Collections.shuffle(points, random);
        int period = 0;
        for (List<Integer> periodPoints : Lists.partition(points, periodSize)) {
            float[][] dist = new float[periodPoints.size()][periodPoints.size()];
            for (int i=0; i<periodPoints.size(); i++) {
                int pi = periodPoints.get(i);
                periods[pi] = period;
                for (int j=0; j<periodPoints.size(); j++) {
                    int pj = periodPoints.get(j);
                    dist[i][j] = problem.getC()[pi][pj];
                }
            }
            period += 1;
            BalancedPMedianVNS vns = new BalancedPMedianVNS(periodPoints.size(), problem.getP(), dist, random);
            vns.run();
            int[] labels = vns.getLabels();
            for (int i=0; i<labels.length; i++)
                medians[periodPoints.get(i)] = periodPoints.get(labels[i]);
        }
        MPBPMPSolution solution = new MPBPMPSolution(periods, medians, 0., 0.);
        double cost = computeObjectiveFunction(solution, problem);
        solution.setObjective(cost);
        return solution;
    }

    public void solveSinglePeriod(MPBPMPSolution solution, MPBPMProblem problem, int period) {
        List<Integer> periodPoints = solution.getPointsInPeriod(period);

        // computing distance submatrix for points in this period
        float[][] dist = new float[periodPoints.size()][periodPoints.size()];
        for (int i=0; i<periodPoints.size(); i++) {
            int pi = periodPoints.get(i);
            for (int j=0; j<periodPoints.size(); j++) {
                int pj = periodPoints.get(j);
                dist[i][j] = problem.getC()[pi][pj];
            }
        }

        // solving the period
        BalancedPMedianVNS vns = new BalancedPMedianVNS(periodPoints.size(), problem.getP(), dist, random);
        vns.run();
        int[] labels = vns.getLabels();

        for (int i=0; i<periodPoints.size(); i++) {
            int point = periodPoints.get(i);
            int newMedian = periodPoints.get(labels[i]);
            solution.setMedian(point, newMedian);
        }

        solution.setObjective(computeObjectiveFunction(solution, problem));
    }

    public double computeObjectiveFunction(MPBPMPSolution solution, MPBPMProblem problem) {
        double distCost = 0, balanceCost = 0;
        double avg = problem.getAvg();
        double alpha = problem.getAlpha();
        float[][] c = problem.getC();
        int[] medians = solution.getMedians();
        Map<Integer, Integer> counts = new HashMap<>();

        for (int i=0; i<problem.getN(); i++) {
            distCost += c[i][medians[i]];
            counts.put(medians[i], counts.getOrDefault(medians[i], 0) + 1);
        }

        for (int count : counts.values())
            balanceCost += alpha * Math.abs(count - avg);

        double cost = distCost + balanceCost;
        LOGGER.info("Computed obj function. obj=" + cost + ", dist=" + distCost + ", balance=" + balanceCost);
        return cost;
    }


}
