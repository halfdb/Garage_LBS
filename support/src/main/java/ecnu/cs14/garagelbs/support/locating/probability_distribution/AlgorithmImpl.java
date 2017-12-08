package ecnu.cs14.garagelbs.support.locating.probability_distribution;

import ecnu.cs14.garagelbs.support.locating.Algorithm;
import ecnu.cs14.garagelbs.support.data.*;

import java.util.*;

import static java.lang.Math.*;

/**
 * An algorithm based on probability distribution.
 * Created by K on 2017/2/14.
 */

public final class AlgorithmImpl extends Algorithm {
    private static final String TAG = AlgorithmImpl.class.getName();
    private ArrayList<Distribution> distributions = new ArrayList<>();
    private ArrayList<Position> locations = new ArrayList<>();

    private boolean closed = false;

    public AlgorithmImpl(MapData map) {
        super(map);
        for (Sample sample :
                map.samples) {
            locations.add(sample.first);
            distributions.add(new Distribution(sample.second));
        }
    }

    public AlgorithmImpl(MapData map, int apThreshold, int locationThreshold) {
        this(map);
        TAILOR_AP_LIMIT = apThreshold;
        TAILOR_LOCATION_LIMIT = locationThreshold;
    }

    private int TAILOR_AP_LIMIT = 8; // q
    private int TAILOR_LOCATION_LIMIT = 15; // k
    @Override
    public Position locate(Fingerprint fingerprint) {
        if (closed) {
            return new Position(0, 0, 0);
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
                    coefficient[i][j] += sqrt(distribution.p(ap, signal)) * sqrt(distributions.get(j).p(ap, signal));
                }
            }
        }

        // calculate the Bhattacharyya distance
        double[] distance = new double[locationCount];
        double logq = log((double) TAILOR_AP_LIMIT < fingerprint.size() ? TAILOR_AP_LIMIT: fingerprint.size());
        for (int i = 0; i < locationCount; i++) {
            for (int j = 0; j < apCount; j++) {
                distance[i] += coefficient[j][i];
            }
            // distance[i] = -log(distance[i] / (double) TAILOR_AP_LIMIT);
            distance[i] = logq - log(distance[i]);
            if (distance[i] <= 1e-5) {
                return locations.get(i);
            }
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
        double z = 0;
        for (int i = 0; i < TAILOR_LOCATION_LIMIT && i < locationCount; i++) {
            Position location = locations.get(chosenLocationIndex[i]);
            x += weight[i] * (double) location.x;
            y += weight[i] * (double) location.y;
            z += weight[i] * (double) location.z;
        }
        x *= factor;
        y *= factor;
        z *= factor;

        return new Position((int) x, (int) y, (int) z);
    }

    @Override
    public void close() throws Exception {
        closed = true;
    }
}
