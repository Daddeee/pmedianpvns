package it.polimi.algorithms.multiperiodbalancedpmedian;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Location;

import java.util.*;

public class HighLevelPMedianTest {
    public static void main(String[] args) {
        int p = 8;
        int m = 30;
        int n = p*m;

        List<Location> locations = new ArrayList<>();
        int xmax = 100;
        int ymax = 100;
        Random random = new Random(13);
        for (int i=0; i<n; i++)
            locations.add(new Location("" + i, random.nextInt(xmax), random.nextInt(ymax)));
        Distance dist = new Euclidean(locations);
        float[][] costs = dist.getDurationsMatrix();

        int[][] periods = new int[n][m];
        for (int i=0; i<m; i++) {
            for (int j=p*i; j<p*(i+1); j++) {
                periods[j][i] = 1;
            }
        }
        List<int[]> periodsList = Arrays.asList(periods);
        Collections.shuffle(periodsList, random);
        int[][] shuffledPeriods = periodsList.toArray(new int[0][0]);

        HighLevelPMedian hpm = new HighLevelPMedian(n, p, m, costs, shuffledPeriods);
        int[] res = hpm.run();
        for (int i=0; i<res.length; i++) {
            int period = -1;
            for (int j=0; j<periods[i].length; j++) {
                if (periods[i][j] == 1) {
                    period = j;
                    break;
                }
            }
            System.out.println("idx=" + i + ", period=" + period + ", median=" + res[i]);
        }
    }
}
