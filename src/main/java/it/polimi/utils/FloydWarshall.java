package it.polimi.utils;

public class FloydWarshall {
    public static float[][] compute(float[][] adj) {
        int n = adj.length;
        float[][] dist = new float[n][n];

        for (int l=0; l<n; l++)
            dist[l] = adj[l].clone();

        for (int k=0; k<n; k++) {
            for (int j=0; j<n; j++) {
                for (int i=0; i<n; i++) {
                    // If vertex k is on the shortest path from
                    // i to j, then update the value of dist[i][j]
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        return dist;
    }
}
