package it.polimi.algorithms.mpbpmp;

import it.polimi.algorithms.balancedpmedian.BalancedFastInterchange;
import it.polimi.algorithms.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithms.mostuniformpmedian.MostUniformPMedianVNS;
import it.polimi.utils.Pair;
import it.polimi.utils.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MPBPMProblemVNDS {
    protected final int MAX_SOLUTION_CHANGES = 100;
    private final Logger LOGGER = LoggerFactory.getLogger(MPBPMProblemVNDS.class);
    private final Random random = new Random();

    private int n;
    private int p;
    private int m;
    private float[][] c;
    private int[] r;
    private int[] d;
    private float alpha;
    private float avg;
    private int kmax;

    private float objective;
    private double elapsedTime;
    private int[] periods;
    private int[] labels;
    private Map<Integer, List<Integer>> periodPoints;
    private Map<Integer, Integer> mediansCount;

    public MPBPMProblemVNDS(int n, int p, int m, float[][] c, int[] r, int[] d) {
        this.n = n;
        this.p = p;
        this.m = m;
        this.c = c;
        this.r = r;
        this.d = d;
        this.alpha = getAlpha(n, c);
        this.avg = (float) n / (p*m);
        this.kmax = getKMax();
    }

    private int getKMax() {
        int count = 0;
        for (int i=0; i<n; i++)
            count += (d[i] - r[i] == 0) ? 0 : 1;
        return Math.max(1, count);
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public float getAlpha() {
        return alpha;
    }

    public int[] getLabels() {
        return labels;
    }

    public int[] getPeriods() {
        return periods;
    }

    public Map<Integer, List<Integer>> getPeriodPoints() {
        return periodPoints;
    }

    public Map<Integer, Integer> getMediansCount() {
        return mediansCount;
    }

    public float getObjective() {
        return objective;
    }

    public void run() {
        // optimal values (TODO: initial solution)
        double start = System.nanoTime();
        int[] periodsOpt = getInitialPeriods();
        Map<Integer, List<Integer>> periodPointsOpt = getPeriodPoints(periodsOpt);
        int[] labelsOpt = getInitialLabels(periodPointsOpt);
        Map<Integer, Integer> mediansCountsOpt = getMediansCount(labelsOpt);
        float fopt = computeObjectiveFunction(labelsOpt, mediansCountsOpt, c);

        // current values
        int[] periodsCur = periodsOpt.clone();
        Map<Integer, List<Integer>> periodPointsCur = new HashMap<>(periodPointsOpt);
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

                LOGGER.info("swapping " + i + " from " + periodsCur[i] + " to " + minPeriod);

                if (!periodPointsCur.get(periodsCur[i]).contains(i))
                    System.out.println("DIO");

                // and update data structures
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
                if (points.size() > 50)
                    System.out.println("DIO");
                BalancedPMedianVNS vns = new BalancedPMedianVNS(points.size(), p, c);
                vns.run();
                int[] solution = vns.getLabels();
                for (int i=0; i<solution.length; i++) {
                    int median = points.get(solution[i]);
                    int point = points.get(i);
                    labelsCur[point] = median;
                    mediansCountsCur.put(median, mediansCountsCur.getOrDefault(median, 0) + 1);
                }
            }
            fcur = computeObjectiveFunction(labelsCur, mediansCountsCur, c);

            LOGGER.info("Done. cur=" + fcur + ", opt=" + fopt);

            // Move or not
            if (fcur < fopt) {
                LOGGER.info("New improving solution founded. Moving.");
                periodsOpt = periodsCur.clone();
                periodPointsOpt = new HashMap<>(periodPointsCur);
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
                periodPointsCur = new HashMap<>(periodPointsOpt);
                mediansCountsCur = new HashMap<>(mediansCountsOpt);
                labelsCur = labelsOpt.clone();
                k = k + 1;
            }
        }

        // save results
        this.objective = fopt;
        this.periods = periodsOpt;
        this.labels = labelsOpt;
        this.periodPoints = new HashMap<>(periodPointsOpt);
        this.mediansCount = new HashMap<>(mediansCountsOpt);

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

    private int[] getInitialPeriods() {
        int[] prds = new int[n];
        for (int i=0; i<n; i++)
            prds[i] = r[i] + random.nextInt(1 + d[i] - r[i]);
        return prds;
    }

    public float computeObjectiveFunction(final int[] labels, Map<Integer,Integer> counts, final float[][] c) {
        float cost = 0;
        for (int i=0; i<n; i++) {
            cost += c[i][labels[i]];
            if (counts.containsKey(i))
                cost += alpha * Math.abs(counts.get(i) - avg);
        }
        return cost;
    }

    public float getAlpha(final int n, final float[][] c) {
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += c[i][j];
                    count += 1;
                }
            }
        }
        return 0.2f * distSum / count;
    }
}
