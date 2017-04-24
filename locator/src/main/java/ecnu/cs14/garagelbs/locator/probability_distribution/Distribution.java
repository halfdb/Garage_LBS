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
    private HashMap<Ap, Double> sampleCounts = new HashMap<>();
    private HashMap<Ap, HashMap<Integer, Integer>> distribution = new HashMap<>();
    private HashMap<Ap, Set<Integer>> signalRanges;
    Distribution(Fingerprint fingerprint) {
        for (Ap ap :
                fingerprint.keySet()) {
            aps.add(ap);
            List<Integer> signals = new ArrayList<>(fingerprint.get(ap));
            HashMap<Integer, Integer> array = Util.count(signals);
            distribution.put(ap, array);
            sampleCounts.put(ap, (double) signals.size());
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
        Integer count = distribution.get(ap).get(signal);
        if (count == null) {
            return 0.0;
        }
        Double total = sampleCounts.get(ap);
        if (total == 0.0) {
            return 0.0;
        }
        return ((double) count) / total;
    }
}
