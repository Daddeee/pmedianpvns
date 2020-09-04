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
                // trying random insertion
                int insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);
                while (insertionPeriod == cur.getPeriods()[toSwap])
                    insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);

                touchedPeriods.add(oldPeriod);
                touchedPeriods.add(insertionPeriod);

                cur.setPeriod(toSwap, insertionPeriod, problem);

                double testcost = computeObjectiveFunction(cur, problem);
                if (Math.abs(cur.getObjective() - testcost) > 0.1)
                    throw new IllegalStateException("Swapping cost (" + cur.getObjective() + ") different from " +
                            "calculated post(" + testcost + ").");
            }

            // TODO local search
            for (int period : touchedPeriods) {
                LOGGER.info("Solving period " + period);
                solveSinglePeriod(cur, problem, period);
                double testcost = computeObjectiveFunction(cur, problem);
                if (Math.abs(cur.getObjective() - testcost) > 0.1)
                    throw new IllegalStateException("Solving cost (" + cur.getObjective() + ") different from " +
                            "calculated post(" + testcost + ").");
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
        List<Integer> periodPoints = solution.getPointsPerPeriod().get(period);
        // compute old cost of period. Needed to update the cost of the solution (cost = cost - oldCost + newCost)
        double oldCost = getPeriodCost(periodPoints, solution, problem);

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

        // updating the solution
        for (int i=0; i<periodPoints.size(); i++) {
            int point = periodPoints.get(i);
            int oldMedian = solution.getMedians()[point];
            solution.getPointsPerMedian().remove(oldMedian);
        }

        for (int i=0; i<periodPoints.size(); i++) {
            int point = periodPoints.get(i);
            int newMedian = periodPoints.get(labels[i]);
            solution.getMedians()[point] = newMedian;
            solution.addToMedian(point, newMedian);
        }

        double newCost = getPeriodCost(periodPoints, solution, problem);

        solution.setObjective(solution.getObjective() - oldCost + newCost);
    }

    private double getPeriodCost(List<Integer> periodPoints, MPBPMPSolution solution, MPBPMProblem problem) {
        double cost = 0.;
        for (int point : periodPoints) {
            int median = solution.getMedians()[point];
            cost += problem.getC()[point][median];
            if (point == median) {
                int size = solution.getPointsPerMedian().get(median).size();
                cost += problem.getAlpha() * Math.abs(size - problem.getAvg());
            }
        }
        return cost;
    }

    /*
    public void run() {
        // optimal values (TODO: initial solution)
        double start = System.nanoTime();
        int[] periodsOpt = getInitialPeriods();
        Map<Integer, List<Integer>> periodPointsOpt = getPeriodPoints(periodsOpt);
        int[] labelsOpt = getInitialLabels(periodPointsOpt);
        Map<Integer, Integer> mediansCountsOpt = getMediansCount(labelsOpt);
        float fopt = computeObjectiveFunction(labelsOpt, mediansCountsOpt, c);
        LOGGER.info("Initial solution. opt=" + fopt);

        // current values
        int[] periodsCur = periodsOpt.clone();
        Map<Integer, List<Integer>> periodPointsCur = cloneMapOfLists(periodPointsOpt);
        int[] labelsCur = labelsOpt.clone();
        Map<Integer, Integer> mediansCountsCur = new HashMap<>(mediansCountsOpt);
        float fcur;
        int k = 1;
        int changes = 0;
        while (k <= kmax) {
            LOGGER.info("Exploring neighborhood of size k=" + k);
            // shaking
            for (int j = 1; j <= k; j++) {
                // sample customer to put in another period
                Integer i = random.nextInt(n);
                while(d[i] - r[i] == 0)
                    i = random.nextInt(n);
                float minCost = Float.MAX_VALUE;
                int minPeriod = periodsCur[i], minMedian = labelsCur[i];
                for (int t=r[i]; t<=d[i]; t++) {
                    if (t == periodsCur[i]) continue;
                    for (int median : periodPointsCur.get(t)) {
                        if (median == labelsCur[median]) { // "median" is really a median
                            float cost = c[i][median] + alpha;
                            cost += alpha * Math.abs(1 + mediansCountsCur.get(median) - avg);
                            cost -= alpha * Math.abs(mediansCountsCur.get(median) - avg);
                            if (cost < minCost) {
                                minCost = cost;
                                minMedian = median;
                                minPeriod = t;
                            }
                        }
                    }
                }
                periodPointsCur.get(periodsCur[i]).remove(i);
                periodPointsCur.get(minPeriod).add(i);
                mediansCountsCur.put(labelsCur[i], mediansCountsCur.get(labelsCur[i]) - 1);
                mediansCountsCur.put(minMedian, mediansCountsCur.get(minMedian) + 1);
                periodsCur[i] = minPeriod;
                labelsCur[i] = minMedian;
            }
            LOGGER.info("Done. Now running local search.");
            // Local search: solve m separate p-median problems by using another vns (can do this in parallel to speed up)
            for (int t=0; t<m; t++) {
                List<Integer> points = periodPointsCur.get(t);
                for (int i : points) {
                    if (labelsCur[i] == i)
                        mediansCountsCur.remove(i);
                }
                BalancedPMedianVNS vns = new BalancedPMedianVNS(points.size(), p, c, 1337);
                vns.run();
                int[] solution = vns.getLabels();
                for (int i=0; i<solution.length; i++) {
                    int median = points.get(solution[i]);
                    int point = points.get(i);
                    labelsCur[point] = median;
                    mediansCountsCur.put(median, mediansCountsCur.getOrDefault(median, 0) + 1);
                }
            }
            LOGGER.info("L");
            fcur = computeObjectiveFunction(labelsCur, mediansCountsCur, c);
            LOGGER.info("Done. cur=" + fcur + ", opt=" + fopt);
            // Move or not
            if (fcur < fopt) {
                LOGGER.info("New improving solution founded. Moving.");
                periodsOpt = periodsCur.clone();
                periodPointsOpt = cloneMapOfLists(periodPointsCur);
                mediansCountsOpt = new HashMap<>(mediansCountsCur);
                labelsOpt = labelsCur.clone();
                fopt = fcur;
                k = 1;
                changes++;
                if (changes >= MAX_SOLUTION_CHANGES) {
                    LOGGER.info("Max solution changes limit hit.");
                    break;
                }
            } else {
                periodsCur = periodsOpt.clone();
                periodPointsCur = cloneMapOfLists(periodPointsOpt);
                mediansCountsCur = new HashMap<>(mediansCountsOpt);
                labelsCur = labelsOpt.clone();
                k = k + 1;
            }
        }
        // save results
        this.objective = fopt;
        this.periods = periodsOpt;
        this.labels = labelsOpt;
        this.periodPoints = periodPointsOpt;
        this.mediansCount = mediansCountsOpt;
        double end = System.nanoTime();
        this.elapsedTime = (end - start) / 1e6;
    }

    private int[] getInitialLabels(Map<Integer, List<Integer>> periodPoints) {
        int[] labels = new int[n];
        for (int t : periodPoints.keySet()) {
            List<Integer> points = periodPoints.get(t);
            Set<Integer> medians = new HashSet<>();
            while(medians.size() < p) {
                medians.add(Rand.sample(points, random));
            }
            for (int point : points) {
                float minDist = Float.MAX_VALUE;
                int minMedian = -1;
                for (int median : medians) {
                    float dist = c[point][median];
                    if (dist < minDist) {
                        minDist = dist;
                        minMedian = median;
                    }
                }
                labels[point] = minMedian;
            }
        }
        return labels;
    }

    private Map<Integer, Integer> getMediansCount(int[] labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<labels.length; i++)
            counts.put(labels[i], counts.getOrDefault(labels[i], 0) + 1);
        return counts;
    }

    private Map<Integer, List<Integer>> getPeriodPoints(int[] prds) {
        Map<Integer, List<Integer>> periodPoints = new HashMap<>();
        for (int i=0; i<n; i++) {
            List<Integer> points = periodPoints.getOrDefault(prds[i], new ArrayList<>());
            points.add(i);
            periodPoints.put(prds[i], points);
        }
        return periodPoints;
    }
    */

    public double computeObjectiveFunction(MPBPMPSolution solution, MPBPMProblem problem) {
        double distCost = 0, balanceCost = 0;
        double avg = problem.getAvg();
        double alpha = problem.getAlpha();
        float[][] c = problem.getC();
        int[] medians = solution.getMedians();
        Map<Integer, List<Integer>> pointsPerMedian = solution.getPointsPerMedian();

        for (int i=0; i<problem.getN(); i++) {
            distCost += c[i][medians[i]];
            if (pointsPerMedian.containsKey(i)) {
                int count = pointsPerMedian.get(i).size();
                balanceCost += alpha * Math.abs(count - avg);
            }
        }
        double cost = distCost + balanceCost;
        LOGGER.info("Computed obj function. obj=" + cost + ", dist=" + distCost + ", balance=" + balanceCost);
        return cost;
    }


}
