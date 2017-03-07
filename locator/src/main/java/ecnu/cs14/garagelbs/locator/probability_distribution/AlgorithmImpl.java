package ecnu.cs14.garagelbs.locator.probability_distribution;

import android.util.Log;
import android.util.Pair;
import ecnu.cs14.garagelbs.locator.Algorithm;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.MapData;
import ecnu.cs14.garagelbs.support.data.Sample;

import java.util.*;

import static java.lang.Math.*;

/**
 * An algorithm based on probability distribution.
 * Created by K on 2017/2/14.
 */

public final class AlgorithmImpl extends Algorithm {
    private static final String TAG = AlgorithmImpl.class.getName();
    private List<Ap> aps;
    private ArrayList<Distribution> distributions = new ArrayList<>();
    private ArrayList<Pair<Integer, Integer>> locations = new ArrayList<>();

    private boolean closed = false;

    public AlgorithmImpl(MapData map) {
        super(map);
        aps = new ArrayList<>(map.aps);
        for (Sample sample :
                map.samples) {
            locations.add(sample.first);
            distributions.add(new Distribution(sample.second));
        }
    }

    private static final int TAILOR_AP_LIMIT = 4;
    private static final int TAILOR_LOCATION_LIMIT = 5;
    @Override
    public Pair<Integer, Integer> locate(Fingerprint fingerprint) {
        if (closed) {
            return new Pair<>(0, 0);
        }
        // tailor fingerprint and make distribution
        Distribution distribution = new Distribution(Util.tailor(fingerprint, TAILOR_AP_LIMIT));

        // calculate the Bhattacharyya coefficients
        List<Ap> aps = distribution.aps;
        int apCount = aps.size();
        int locationCount = locations.size();
        double[][] coefficient = new double[apCount][locationCount];
        for (int i = 0; i < apCount; i++) {
            Ap ap = aps.get(i);
            Collection<Integer> signalRange = distribution.getSignalRange(ap);
            for (int j = 0; j < locationCount; j++) {
                for (int signal :
                        signalRange) {
                    coefficient[i][j] += sqrt(distribution.p(ap, signal) * distributions.get(j).p(ap, signal));
                }
            }
        }

        // calculate the Bhattacharyya distance
        double[] distance = new double[locationCount];
        for (int i = 0; i < locationCount; i++) {
            for (int j = 0; j < apCount; j++) {
                distance[i] += coefficient[j][i];
            }
            distance[i] = -log(distance[i] / (double) TAILOR_AP_LIMIT);
        }

        // choose some nearest locations
        double[] sortedDistance = Arrays.copyOf(distance, distance.length);
        int[] chosenLocationIndex = new int[TAILOR_LOCATION_LIMIT];
        Arrays.sort(sortedDistance);
        for (int i = 0; i < TAILOR_LOCATION_LIMIT && i < locationCount; i++) {
            for (int j = 0; j < distance.length; j++) {
                if (distance[j] == sortedDistance[i]) {
                    chosenLocationIndex[i] = j;
                    break;
                }
            }
        }

        // calculate the weight and the normalization factor
        double[] weight = new double[TAILOR_LOCATION_LIMIT];
        double factor = 0.0;
        for (int i = 0; i < TAILOR_LOCATION_LIMIT && i < locationCount; i++) {
            weight[i] = 1.0 / distance[chosenLocationIndex[i]];
            factor += weight[i];
        }
        factor = 1.0 / factor;

        // estimate the coordinate
        double x = 0;
        double y = 0;
        for (int i = 0; i < TAILOR_LOCATION_LIMIT && i < locationCount; i++) {
            Pair<Integer, Integer> location = locations.get(chosenLocationIndex[i]);
            x += weight[i] * (double) location.first;
            y += weight[i] * (double) location.second;
        }
        x *= factor;
        y *= factor;

        Log.i(TAG, "locate: x: " + x + " y: " + y);

        return new Pair<>((int) x, (int) y);
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }
}
