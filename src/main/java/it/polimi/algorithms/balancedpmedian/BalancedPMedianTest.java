package it.polimi.algorithms.balancedpmedian;

import it.polimi.domain.Location;
import it.polimi.io.LatLngCSVReader;

import java.util.List;

public class BalancedPMedianTest {
    public static void main(String[] args) {
        List<Location> locations = LatLngCSVReader.read("instances/speedy/grosseto-test.csv");
        int p = 5;
        BalancedPMedianExact.solve(locations, p);
        BalancedPMedianRun.solve(locations, p);
    }
}
