package ecnu.cs14.garagelbs.locator.probability_distribution;

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
    private HashMap<Ap, HashMap<Integer, Integer>> distribution = new HashMap<>();
    private HashMap<Ap, Set<Integer>> signalRanges;
    Distribution(Fingerprint fingerprint) {
        sampleCount = (double) fingerprint.sampleCount;
        for (Ap ap :
                fingerprint.keySet()) {
            aps.add(ap);
            List<Integer> signals = new ArrayList<>(fingerprint.get(ap));
            HashMap<Integer, Integer> array = Util.count(signals);
            distribution.put(ap, array);
        }
    }

    Collection<Integer> getSignalRange(Ap ap) {
        if (signalRanges == null) {
            signalRanges = new HashMap<>();
        }
        if (!signalRanges.containsKey(ap)) {
            HashMap<Integer, Integer> array = distribution.get(ap);
            Set<Integer> signalRange = array.keySet();
            signalRanges.put(ap, signalRange);
            return signalRange;
        }
        return signalRanges.get(ap);
    }

    double p(Ap ap, int signal) {
        return ((double) distribution.get(ap).get(signal)) / sampleCount;
    }
}
