package it.polimi.distances;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.polimi.domain.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OSRM extends Distance {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSRM.class);
    private static final String BASE_OSRM_URL = "https://vrpsolver.ermes-x.com/osrm/car/table/v1/driving/";

    private float[][] distancesMatrix;
    private float[][] durationsMatrix;

    private Map<String, Integer> locationIndices;

    public OSRM(List<Location> locations) {
        super(locations);
        this.locationIndices = new HashMap<>();
        for (int i = 0; i < locations.size(); i++)
            locationIndices.put(locations.get(i).getId(), i);
    }

    @Override
    public float[][] getDistancesMatrix() {
        return distancesMatrix;
    }

    @Override
    public float[][] getDurationsMatrix() {
        return durationsMatrix;
    }


    @Override
    protected void buildMatrices(List<Location> locations) {
        LOGGER.info("Populating matrix with " + locations.size() + " locations.");
        StringBuilder urlBuilder = new StringBuilder(BASE_OSRM_URL);

        boolean first = true;
        for (Location l : locations) {
            if (!first) {
                urlBuilder.append(';');
            } else {
                first = false;
            }
            urlBuilder
                    .append(l.getLng())
                    .append(',')
                    .append(l.getLat());
        }

        urlBuilder.append("?annotations=distance,duration&exclude=ferry");

        try {
            LOGGER.info("Retrieving data from server.");
            URL url = new URL(urlBuilder.toString());
            InputStream stream = url.openStream();
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(new InputStreamReader(stream));
            LOGGER.info("Retrieved data.");

            JsonArray durations = root.getAsJsonObject().get("durations").getAsJsonArray();
            this.durationsMatrix = getMatrix(durations);
            LOGGER.info("Retrieved durations matrix. size=(" + this.durationsMatrix.length + "," + this.durationsMatrix[0].length + ")");

            JsonArray distances = root.getAsJsonObject().get("distances").getAsJsonArray();
            this.distancesMatrix = getMatrix(distances);
            LOGGER.info("Retrieved distances matrix. shape=(" + this.durationsMatrix.length + "," + this.durationsMatrix[0].length + ")");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private float[][] getMatrix(final JsonArray arr) {
        float[][] matrix = new float[arr.size()][];
        for (int i = 0; i < arr.size(); i++) {
            JsonArray inner = arr.get(i).getAsJsonArray();
            matrix[i] = new float[inner.size()];
            for (int j = 0; j < inner.size(); j++) {
                if(inner.get(j).isJsonNull()) {
                    matrix[i][j] = Float.MAX_VALUE;
                } else {
                    matrix[i][j] = inner.get(j).getAsFloat();
                }
            }
        }
        return matrix;
    }

}
