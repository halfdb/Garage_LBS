package ecnu.cs14.garagelbs.locator.probability_distribution;

import android.util.SparseIntArray;
import ecnu.cs14.garagelbs.support.data.Ap;
import ecnu.cs14.garagelbs.support.data.Fingerprint;
import ecnu.cs14.garagelbs.support.data.Sample;

import java.util.*;

/**
 * Representing probability distributions of all APs' signals at a location.
 * Created by K on 2017/2/14.
 */

final class Distribution {
    List<Ap> aps = new ArrayList<>();
    private double sampleCount;
    private HashMap<Ap, SparseIntArray> distribution = new HashMap<>();
    private HashMap<Ap, Set<Integer>> signalRanges;
    Distribution(Fingerprint fingerprint) {
        sampleCount = (float) fingerprint.sampleCount;
        for (Ap ap :
                fingerprint.keySet()) {
            aps.add(ap);
            List<Integer> signals = new ArrayList<>(fingerprint.get(ap));
            SparseIntArray array = Util.count(signals);
            distribution.put(ap, array);
        }
    }

    Collection<Integer> getSignalRange(Ap ap) {
        if (signalRanges == null) {
            signalRanges = new HashMap<>();
        }
        if (!signalRanges.containsKey(ap)) {
            SparseIntArray array = distribution.get(ap);
            HashSet<Integer> signalRange = new HashSet<>();
            for (int i = 0; i < array.size(); i++) {
                signalRange.add(array.keyAt(i));
            }
            signalRanges.put(ap, signalRange);
            return signalRange;
        }
        return signalRanges.get(ap);
    }

    double p(Ap ap, int signal) {
        return ((double) distribution.get(ap).get(signal)) / sampleCount;
    }
}
